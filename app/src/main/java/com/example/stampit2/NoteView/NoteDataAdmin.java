package com.example.stampit2.NoteView;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;


import com.example.stampit2.Utils.FileUtils;
import com.example.stampit2.Utils.InformUtils;
import com.example.stampit2.databinding.InformOnlyInfoBinding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class NoteDataAdmin
{
    private final String rootPath;
    private final String metaPath;
    private final String pagePath;

    private File getSummaryMetaFile()
    { return new File(metaPath, "sum.met");}
    private File getPageMetaFile()
    { return new File(metaPath, "page.met");}
    private File getSparePageMetaFile()
    { return new File(metaPath, "spare.met");}

    private File getNotePageFile(short pageFile)
    { return new File(pagePath, String.valueOf(noteInfo.pages.get(pageFile))); }

    private File getNotePageVersionFile(short page, short version)
    { return new File(pagePath + "/"
            + noteInfo.pages.get(page) + "/v/" + String.valueOf(version));}



    private NoteInfo noteInfo;
    public short getPageSize() { return (short) noteInfo.pages.size();}


    private Context appContext;


    public NoteDataAdmin(Context context, String inRootPath, boolean isCreating)
    {
        appContext = context.getApplicationContext();

        rootPath = inRootPath;
        metaPath = rootPath + "/m";
        pagePath = rootPath + "/p";

        noteInfo = new NoteInfo();
        if(isCreating)
        {
            createNote();
        }
        else
        {
            loadNoteFromMeta();
        }
    }










    private void createNote()
    {
        noteInfo.pages = new ArrayList<Short>();
        noteInfo.sparePages = new ArrayList<Short>();
        noteInfo.lastId = 0;

        new File(pagePath).mkdirs();
        new File(metaPath).mkdirs();

        saveAllMeta();
    }


    private void saveAllMeta()
    {
        saveSummaryMeta();
        savePageMeta();
        saveSparePageMeta();
    }

    private void saveSummaryMeta()
    {
        try(DataOutputStream dos
                    = new DataOutputStream(new FileOutputStream(getSummaryMetaFile())))
        {
            dos.writeShort(noteInfo.lastId);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            InformUtils.instance().showOnlyInfo(appContext,
                    "오류 : NoteDataAdmin - saveSummaryMeta");
        }
    }
    private void savePageMeta()
    {
        try(DataOutputStream dos
                = new DataOutputStream(new FileOutputStream(getPageMetaFile())))
        {
            dos.writeInt(noteInfo.pages.size());
            for(short id : noteInfo.pages)
            {
                dos.writeShort(id);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            InformUtils.instance().showOnlyInfo(appContext,
                    "오류 : NoteDataAdmin - savePageMeta");
        }
    }
    private void saveSparePageMeta()
    {
        try(DataOutputStream dos
                = new DataOutputStream(new FileOutputStream(getSparePageMetaFile())))
        {
            dos.writeInt(noteInfo.sparePages.size());
            for(short id : noteInfo.sparePages)
            {
                dos.writeShort(id);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            InformUtils.instance().showOnlyInfo(appContext,
                    "오류 : NoteDataAdmin - saveSparePageMeta");
        }
    }


    private void loadNoteFromMeta()
    {
        try
        {
            try(DataInputStream dis
                        = new DataInputStream(new FileInputStream(getSummaryMetaFile())))
            {
                noteInfo.lastId = dis.readShort();
            }
            try(DataInputStream dis
                        = new DataInputStream(new FileInputStream(getPageMetaFile())))
            {
                int pageSize = dis.readInt();
                noteInfo.pages = new ArrayList<>(pageSize);
                while(pageSize-- > 0)
                {
                    noteInfo.pages.add(dis.readShort());
                }
            }
            try(DataInputStream dis
                    = new DataInputStream(new FileInputStream(getSparePageMetaFile())))
            {
                int pageSize = dis.readInt();
                noteInfo.sparePages = new ArrayList<>(pageSize);
                while(pageSize-- > 0)
                {
                    noteInfo.sparePages.add(dis.readShort());
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            InformUtils.instance().showOnlyInfo(appContext,
                    "오류 : NoteDataAdmin - loadNoteFromMeta");
        }

    }





    public short insertPage(Uri uri, short currPage, boolean isNext)
    {
        currPage = noteInfo.pages.size() == 0 ? (short) 0
                : (short) (isNext ? currPage + 1 : currPage);

        saveNoteByPage(uri, noteInfo.lastId);

        noteInfo.pages.add(currPage, noteInfo.lastId++);

        saveSummaryMeta();
        savePageMeta();

        return currPage;
    }


    private void saveNoteByPage(Uri uri, short page)
    {
        // TODO : 버전에 대한 메타 데이터 생성

        File versionFile = getNotePageVersionFile(page, (short) 1);
        versionFile.mkdirs();
        File imageFile = new File(versionFile, getOriginName(uri) + ".img");


        try
        {
            InputStream inputStream = appContext.getContentResolver()
                    .openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int len;
            while((len = inputStream.read(buffer)) > 0)
            {
                fos.write(buffer, 0, len);
            }

            fos.flush();
            fos.close();
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            InformUtils.instance().showOnlyInfo(appContext,
                    "오류 : NoteDataAdmin - saveNoteByPage");
        }

    }


    public void deletePage(short page)
    {
        noteInfo.sparePages.add(noteInfo.pages.get(page));

        File deletedFile = getNotePageFile(page);
        FileUtils.instance().deleteFile(deletedFile);

        noteInfo.pages.remove(page);

        savePageMeta();
        saveSparePageMeta();
    }































    public Uri loadNoteByPage(short page)
    {
        // TODO : 버전에 대한 메타 데이터에 기반하여 마지막 버전의 img 를 리턴

        File versionFile = getNotePageVersionFile(noteInfo.pages.get(page), (short) 1);

        if(versionFile.exists() == false || versionFile.isDirectory() == false)
        {
            InformUtils.instance().showOnlyInfo(appContext,
                    "오류 : NoteDataAdmin - loadNoteByPage 1");
            return null;
        }

        // 폴더 내에서 .img 로 끝나는 첫번째 파일을 리턴
        File[] imageFiles = versionFile.listFiles((dir, name) -> name.endsWith(".img"));
        if(imageFiles == null || imageFiles.length == 0)
        {
            InformUtils.instance().showOnlyInfo(appContext,
                    "오류 : NoteDataAdmin - loadNoteByPage 2");
            return null;
        }

        return Uri.fromFile(imageFiles[0]);
    }


    private String getOriginName(Uri uri)
    {
        try (Cursor cursor = appContext.getContentResolver().query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null))
        {
            if (cursor != null && cursor.moveToFirst())
            {
                return cursor.getString(
                        cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "unknown";
    }



}
