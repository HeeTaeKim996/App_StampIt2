package com.example.stampit2.NoteView.PagesView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stampit2.NoteView.NoteDataAdmin;
import com.example.stampit2.R;

public class PagesView extends LinearLayout
{
    private RecyclerView recyclerView;


    public PagesView(Context context)
    { super(context); }

    public PagesView(Context context, AttributeSet attrs)
    { super(context, attrs); }

    public PagesView(Context context, AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        recyclerView = findViewById(R.id.recyclerView_pages);
    }

    public void initialize(NoteDataAdmin dataAdmin)
    {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(new PagesAdapter(dataAdmin));
    }

}
