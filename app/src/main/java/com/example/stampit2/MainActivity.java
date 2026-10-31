package com.example.stampit2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.stampit2.Dirs.DB.DbRepository;

//public class MainActivity extends AppCompatActivity
//{
//    private NoteInterface noteInterface;
//
//    private boolean tempIsNext;
//    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult
//            (
//            new ActivityResultContracts.GetMultipleContents(),
//            uris->
//            {
//                if(noteInterface != null)
//                {
//                    noteInterface.insertPage(uris, tempIsNext);
//                }
//            }
//    );
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//
////        FileUtils.instance().clearAllFiles(this);
////        InformUtils.instance().showOnlyInfo(this, FileUtils.instance().getAllFiles(this).toString());
//
//
//        setContentView(R.layout.note);
//
//        noteInterface = findViewById(R.id.noteInterface);
//        noteInterface.setOnAddPhotoClicked((boolean isNext)->
//        {
//            tempIsNext = isNext;
//            galleryLauncher.launch("image/*");
//        });
//
//
//        File tempFile = new File(getFilesDir(), "TempNote");
//        if(tempFile.exists())
//        {
//            noteInterface.initializeNote(tempFile.getPath(), false);
//        }
//        else
//        {
//            noteInterface.initializeNote(tempFile.getPath(), true);
//        }
//    }
//}




public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

//        deleteDatabase("dirs.db");

        DbRepository.initialize(this);




        setContentView(R.layout.dirs);
    }
}