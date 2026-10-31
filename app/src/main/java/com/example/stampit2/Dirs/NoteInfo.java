package com.example.stampit2.Dirs;

public class NoteInfo extends FileInfo
{
    public NoteInfo(long inId,  String inTitle, String inImagePath)
    {
        super(inId, inTitle);
        imagePath = inImagePath;
    }

    @Override
    public int getType()
    {
        return TYPE_NOTE;
    }

    public String getImagePath() { return imagePath; }


    private  String imagePath;


}
