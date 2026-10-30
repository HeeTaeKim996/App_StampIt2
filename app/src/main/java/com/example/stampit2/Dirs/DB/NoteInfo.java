package com.example.stampit2.Dirs.DB;

public class NoteInfo
{
    public NoteInfo(long inId,  String inTitle, String inPreview, long inUpdatedAt)
    {
        id = inId;
        title = inTitle;
        preview = inPreview;
        updatedAt = inUpdatedAt;
    }
    public long id;
    public String title;
    public String preview;
    public long updatedAt;
}
