package com.example.stampit2.Dirs;

public class FolderInfo extends FileInfo
{
    public FolderInfo(long inId, long inParentId, String inTitle)
    {
        super(inId, inTitle);
        parentId = inParentId;
    }

    private long parentId;

    @Override
    public int getType()
    {
        return TYPE_FOLDER;
    }
}
