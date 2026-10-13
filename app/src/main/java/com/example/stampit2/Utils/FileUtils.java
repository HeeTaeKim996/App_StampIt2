package com.example.stampit2.Utils;

import android.content.Context;

import java.io.File;

public class FileUtils
{
    private static FileUtils _instance;
    private FileUtils(){}
    public static FileUtils instance()
    {
        if(_instance == null)
        {
            _instance = new FileUtils();
        }
        return _instance;
    }




    public void clearAllFiles(Context context)
    {
        deleteChildrenFiles(context.getFilesDir());
    }
    private void deleteChildrenFiles(File me)
    {
        File[] children = me.listFiles();
        for(File child : children)
        {
            if(child.isFile() == false)
            {
                deleteChildrenFiles(child);
            }
            child.delete();
        }
    }



    public StringBuilder getAllFiles(Context context)
    {
        StringBuilder sb = new StringBuilder(".\n\n ALL FILES \n\n");
        listAllFiles(sb, "", context.getFilesDir());
        return sb;
    }

    private void listAllFiles(StringBuilder sb, String parent, File dir)
    {
        String me = parent + "/" + dir.getName();
        sb.append(me + "\n");

        File[] children = dir.listFiles();
        for(File child : children)
        {
            if(child.isFile() == false)
            {
                listAllFiles(sb, me, child);
            }
            else
            {
                sb.append(me + "/" + child.getName() + "\n");
            }
        }
    }
}
