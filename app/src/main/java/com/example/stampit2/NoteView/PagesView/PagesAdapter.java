package com.example.stampit2.NoteView.PagesView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.stampit2.NoteView.NoteDataAdmin;
import com.example.stampit2.R;

import java.util.List;

public class PagesAdapter extends RecyclerView.Adapter<PagesAdapter.PageViewHolder>
{
    public static class PageViewHolder extends RecyclerView.ViewHolder
    {
        ImageView pageImage;
        TextView pageNumber;

        public PageViewHolder(View itemView)
        {
            super(itemView);

            pageImage = itemView.findViewById(R.id.imageView_pages_item_page);
            pageNumber = itemView.findViewById(R.id.textView_pages_item_page);
        }
    }

    private final NoteDataAdmin dataAdmin;

    public PagesAdapter(NoteDataAdmin inDataAdmin)
    {
        dataAdmin = inDataAdmin;
    }


    @Override
    public PageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pages_item_page, parent,
                false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PageViewHolder holder, int position)
    {
        holder.pageImage.setImageURI(dataAdmin.loadNoteByPage((short)position));
        holder.pageNumber.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount()
    {
        return dataAdmin.getPageSize();
    }
}
