package com.example.stampit2.Dirs;

import java.io.File;

public class HeaderInfo extends FileInfo
{

    public HeaderInfo(long inId, String inTitle)
    {
        super(inId, inTitle);
    }

    @Override
    public int getType()
    {
        return FileInfo.TYPE_HEADER;
    }
}
