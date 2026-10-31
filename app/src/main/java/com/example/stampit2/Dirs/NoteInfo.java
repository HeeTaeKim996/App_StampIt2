package com.example.stampit2.Dirs;

public class NoteInfo extends FileInfo
{
    public NoteInfo(long inId,  String inTitle)
    {
        super(inId, inTitle);
    }

    @Override
    public int getType()
    {
        return TYPE_NOTE;
    }


}
