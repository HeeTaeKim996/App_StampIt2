package com.example.stampit2.NoteView.PagesView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stampit2.NoteView.NoteDataAdmin;
import com.example.stampit2.R;

public class PagesView extends LinearLayout
{
    private RecyclerView recyclerView;
    private TextView pagesInfo;
    private Button selectButton;
    private PagesAdapter adapter;

    private Runnable closeViewListener;
    private LinearLayout bottomLayout;

    private Button deleteButton;


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
        pagesInfo = findViewById(R.id.textView_pages_pageInfo);
        selectButton = findViewById(R.id.button_pages_selectPages);
        bottomLayout = findViewById(R.id.linearLayout_pages_bottom);
        deleteButton = findViewById(R.id.button_pages_deletePages);
    }

    public void initialize(NoteDataAdmin dataAdmin, short currPage, Runnable inCloseViewListener)
    {
        closeViewListener = inCloseViewListener;

        bottomLayout.setVisibility(GONE); // GONE : 안보이면서, 공간 점유도 안함

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));


        adapter = new PagesAdapter(this, dataAdmin, currPage);
        recyclerView.setAdapter(adapter);

        selectButton.setOnClickListener(v->
        {
            if(adapter.isSelectMode())
            {
                selectButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.purple_500));
                adapter.onSelectButtonClicked(false);
                bottomLayout.setVisibility(GONE);
            }
            else
            {
                selectButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_green));
                adapter.onSelectButtonClicked(true);
                bottomLayout.setVisibility(VISIBLE);
            }
        });

        deleteButton.setOnClickListener(v->
        {
            adapter.onDeleteButtonClicked();
        });
    }



    public void updatePagesInfo(short currPage, short pagesCount)
    {
        pagesInfo.setText((currPage + 1) + " / " + pagesCount);
    }

    public short getCurrPage()
    {
        return adapter.getCurrPage();
    }

    public void closeView()
    {
        closeViewListener.run();
    }
}
