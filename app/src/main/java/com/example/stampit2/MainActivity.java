package com.example.stampit2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.example.stampit2.NoteView.NoteInterface;

public class MainActivity extends AppCompatActivity
{
    private NoteInterface noteInterface;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult
            (
            new ActivityResultContracts.GetContent(),
            uri->
            {
                if(uri != null && noteInterface != null)
                {
                    noteInterface.setImage((uri));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        noteInterface = findViewById(R.id.noteInterface);
        noteInterface.setOnAddPhotoClicked(()->
        {
            galleryLauncher.launch("image/*");
        });


    }
}