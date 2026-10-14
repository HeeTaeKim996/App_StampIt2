package com.example.stampit2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.stampit2.NoteView.NoteInterface;
import com.example.stampit2.Utils.FileUtils;
import com.example.stampit2.Utils.InformUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
    private NoteInterface noteInterface;

    private boolean tempIsNext;
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult
            (
            new ActivityResultContracts.GetContent(),
            uri->
            {
                if(uri != null && noteInterface != null)
                {
                    noteInterface.insertPage(uri, tempIsNext);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        FileUtils.instance().clearAllFiles(this);

//        InformUtils.instance().showOnlyInfo(this, FileUtils.instance().getAllFiles(this).toString());


        setContentView(R.layout.note);

        noteInterface = findViewById(R.id.noteInterface);
        noteInterface.setOnAddPhotoClicked((boolean isNext)->
        {
            tempIsNext = isNext;
            galleryLauncher.launch("image/*");
        });


        noteInterface.initializeNote(new File(getFilesDir(), "TempNote"));
    }
}