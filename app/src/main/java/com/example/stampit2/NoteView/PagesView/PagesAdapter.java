package com.example.stampit2.NoteView.PagesView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stampit2.NoteView.NoteDataAdmin;
import com.example.stampit2.R;
import com.example.stampit2.Utils.InformUtils;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.function.Consumer;

public class PagesAdapter extends RecyclerView.Adapter<PagesAdapter.PageViewHolder>
{

    private final PagesView pagesView;
    private final NoteDataAdmin dataAdmin;
    private ItemTouchHelper touchHelper;

    private short currPage;
    public short getCurrPage() { return currPage;}

    private boolean isSelectMode = false;
    private TreeSet<Short> selected;

    public PagesAdapter(PagesView inPagesView, NoteDataAdmin inDataAdmin, short inCurrPage)
    {
        pagesView = inPagesView;
        dataAdmin = inDataAdmin;
        currPage = inCurrPage;

        pagesView.updatePagesInfo(currPage, dataAdmin.getPageSize());
        selected = new TreeSet<>();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);

        PageDragCallback callback = new PageDragCallback(this);

        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView)
    {
        super.onDetachedFromRecyclerView(recyclerView);

        if(touchHelper != null)
        {
            touchHelper.attachToRecyclerView(null);
            touchHelper = null;
        }
    }



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

        if(isSelectMode == false)
        {
            holder.itemView.setSelected(false);
            holder.itemView.setActivated(position == currPage);
        }
        else
        {
            holder.itemView.setSelected(selected.contains((short)position));
            holder.itemView.setActivated(false);
        }

        holder.itemView.setOnClickListener(v->
        {
            int pos = (short) holder.getAdapterPosition();
            if(pos == RecyclerView.NO_POSITION) return;
            short shortPos = (short) pos;

            if(isSelectMode == false)
            {
                currPage = shortPos;
                pagesView.closeView();
            }
            else
            {
                if(selected.contains(shortPos))
                {
                    selected.remove(shortPos);
                }
                else
                {
                    selected.add(shortPos);
                }

                notifyItemChanged(pos);
            }

        });
    }

    @Override
    public int getItemCount()
    {
        return dataAdmin.getPageSize();
    }


    public void saveOnItemMoved(int from, int to)
    {
        dataAdmin.movePage((short)from, (short)to);

        if(currPage == from)
        {
            currPage = (short) to;
        }
        else if(from < currPage && currPage <= to)
        {
            currPage--;
        }
        else if(currPage < from && to <= currPage)
        {
            currPage++;
        }

        int min = Math.min(from, to);
        notifyItemRangeChanged(min, Math.abs(from - to) + 1);
        pagesView.updatePagesInfo(currPage, dataAdmin.getPageSize());
    }


    public boolean isSelectMode() { return isSelectMode; }
    public void onSelectButtonClicked(boolean set)
    {
        isSelectMode = set;

        if(isSelectMode == false)
        {
            for(short s : selected)
            {
                notifyItemChanged(s);
            }
            selected.clear();
        }


        notifyItemChanged(currPage);
    }

    public void onDeleteButtonClicked()
    {
        if(isSelectMode == false || selected.size() == 0) return;


        Consumer<Boolean> bYes = (Boolean isYes)->
        {
            if(isYes)
            {
                ArrayList<Short> deletingPages = new ArrayList<>(selected);
                selected.clear();

                dataAdmin.deletePages(deletingPages);

                short pageSize = dataAdmin.getPageSize();
                if(pageSize > 0)
                {
                    int removingCount = 0;
                    for(int i = 0; i < deletingPages.size() && deletingPages.get(i) <= currPage;
                        ++i, ++removingCount) { }

                    if(currPage < removingCount)
                    {
                        currPage = 0;
                    }
                    else
                    {
                        currPage -= removingCount;
                    }
                }
                else
                {
                    currPage = 0;
                }

                notifyDataSetChanged();
                pagesView.updatePagesInfo(currPage, pageSize);
            }
        };


        InformUtils.instance().showYesOrNo(pagesView.getContext(),
                "선택한 페이지들을 모두 삭제하겠습니까?", bYes);
    }


}
