package com.example.stampit2.Dirs;

public abstract class FileInfo
{
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_NOTE = 2;

    public FileInfo(long inId, String inTitle)
    {
        id = inId;
        title = inTitle;
    }


    public abstract int getType();

    public long getId() { return id;}
    public String getTitle() { return title;}


    protected long id;
    protected String title;

}
