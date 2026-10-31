package com.example.stampit2.Dirs.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper
{

    private static final String DB_NAME = "dirs.db";
    private static final int DB_VERSION = 1;

    public DbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION); // (2) 는 고정 null 이라 보면 된다. 이해하지 말자
    }

    // 매번 호출되는 게 아니라, 생성자의 DB_NAME 이 처음 등록될 때만 호출
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE folders(" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "parentId INTEGER NOT NULL DEFAULT 0," +
                        "name TEXT NOT NULL" +
                        ")"
                );
        db.execSQL("CREATE INDEX idx_folders_parent ON folders(parentId)");

        db.execSQL("CREATE TABLE notes(" +
                        "id INTEGER PRIMARY KEY," +
                        "folderId INTEGER NOT NULL DEFAULT 0," +
                        "title TEXT NOT NULL," +
                        "imagePath TEXT" +
                        ")"
                );
        db.execSQL("CREATE INDEX idx_notes_folder ON notes(folderId)");
    }

    // DB_VERSION 이 올라갔을 때 호출 (스키마 변경 시)
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {
        // 예: if (oldVersion < 2) db.execSQL("ALTER TABLE notes ADD COLUMN pinned INTEGER DEFAULT 0");
    }

    // DB 에 연결될 때, onCreate/onUpgrade/onOpen 보다 먼저 호출됨
    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
