package com.example.stampit2.Dirs.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;


import com.example.stampit2.Dirs.FileInfo;
import com.example.stampit2.Dirs.FolderInfo;
import com.example.stampit2.Dirs.HeaderInfo;
import com.example.stampit2.Dirs.NoteInfo;

import java.util.ArrayList;
import java.util.List;

public class DbRepository
{
    private static DbRepository _instance;
    private DbHelper helper;

    public static void initialize(Context context)
    {
        if(_instance != null) return;
        _instance = new DbRepository(context);
    }

    public static DbRepository instance()
    {
        return _instance;
    }

    private DbRepository(Context context)
    {
        helper = new DbHelper(context.getApplicationContext());
    }

    // 폴더 생성
    public long insertFolder(long parentId, String name) // SQLite 의 INTEGER 는 long(8)
    {
        // 리턴값(id)를 받기 위해 stmt 사용
        SQLiteStatement stmt = helper.getWritableDatabase().compileStatement(
                "INSERT INTO folders (parentId, name) VALUES(?, ?)");

        try
        {
            stmt.bindLong(1, parentId);
            stmt.bindString(2, name);
            return stmt.executeInsert();
        }
        finally // try 에 리턴이 호출돼도, finally 가 return 전에 호출됨
        {
            stmt.close();
        }
    }

    public List<FolderInfo> getAllFolders()
    {
        List<FolderInfo> list = new ArrayList<>();
        try(Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, parentId, name FROM folders", null))
        {
            while(c.moveToNext())
            {
                list.add(new FolderInfo(c.getLong(0), c.getLong(1), c.getString(2)));
            }
        }
        return list;
    }

    public List<FileInfo> getFilesInFolder(long folderId)
    {
        List<FileInfo> list = new ArrayList<>();

        try(Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, parentId, name FROM folders " +
                        "WHERE parentId = ? ORDER BY id ASC",
                new String[]{String.valueOf(folderId)}
        ))
        {
            while(c.moveToNext())
            {
                list.add(new FolderInfo(c.getLong(0), c.getLong(1), c.getString(2)));
            }
        }

        list.add(new HeaderInfo(0, ""));

        try(Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT id, title, imagePath FROM notes " +
                        "WHERE folderId = ? ORDER BY id DESC",
                new String[]{String.valueOf(folderId)}
        ))
        {
            while(c.moveToNext())
            {
                list.add(new NoteInfo(c.getLong(0), c.getString(1), c.getString(2)));
            }
        }

        return list;
    }


    public void moveNote(long noteId, long newFolderId)
    {
        helper.getWritableDatabase().execSQL(
                "UPDATE notes SET folderId = ? " +
                        "WHERE id = ?",
                new String[]{String.valueOf(newFolderId), String.valueOf(noteId)}
        );
    }

    public void deleteNote(long noteId)
    {
        // 삭제는 메타 먼저, 실제 파일은 이후 삭제를 원칙
        
        helper.getWritableDatabase().execSQL(
                "DELETE FROM notes " +
                        "WHERE id = ?",
                new String[]{String.valueOf(noteId)}
        );
        
        // TODO : 실제 파일 삭제
    }

    public long getFoldersParentId(long id)
    {
        try(Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT parentId FROM folders " +
                        "WHERE id = ?",
                new String[]{String.valueOf(id)}
        ))
        {
            if(c.moveToNext())
            {
                return c.getLong(0);
            }
        }

        return 0;
    }
}
