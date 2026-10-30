package com.example.stampit2.Dirs.DB;

public class FolderInfo
{
    public FolderInfo(long inId, long inParentId, String inName)
    {
        id = inId;
        parentId = inParentId;
        name = inName;
    }

    public long id;
    public long parentId;
    public String name;
}
