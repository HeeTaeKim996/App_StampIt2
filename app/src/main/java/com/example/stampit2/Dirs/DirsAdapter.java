package com.example.stampit2.Dirs;


import android.app.LauncherActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stampit2.R;

import java.util.Objects;
import java.util.function.BiConsumer;

public class DirsAdapter extends ListAdapter<FileInfo, RecyclerView.ViewHolder>
{
    private BiConsumer<FromDirsAdapterEnum, Long> fromDirsAdapter = null;
    public void setFromDirsAdapter(BiConsumer<FromDirsAdapterEnum, Long> inFrom)
    { fromDirsAdapter = inFrom;}

    public DirsAdapter()
    {
        super(new DiffUtil.ItemCallback<FileInfo>()
        {
            @Override
            public boolean areItemsTheSame(FileInfo oldItem, FileInfo newItem)
            {
                // FALSE 시, 뷰가 사라지는 애니매이션과 함께, 풀 에 같은 타입의 뷰가 있는지 확인. 없다면
                // createViewHolder 호출 후 뷰를 가져온 후, onBindViewHolder 호출
                return oldItem.getType() == newItem.getType()
                        && oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(FileInfo oldItem, FileInfo newItem)
            {
                // FALSE 시, notifyItemChanged 와 같은 효과 (onBindViewHolder 호출)

                if(oldItem instanceof FolderInfo && newItem instanceof FolderInfo)
                {
                    return ((FolderInfo) oldItem).getTitle().equals(((FolderInfo) newItem).getTitle());
                }
                else if(oldItem instanceof NoteInfo && newItem instanceof NoteInfo)
                {
                    NoteInfo o = (NoteInfo) oldItem;
                    NoteInfo n = (NoteInfo) newItem;
                    return Objects.equals(o.getTitle(), n.getTitle())
                            && Objects.equals(o.getImagePath(), n.getImagePath());
                }
                else if(oldItem instanceof HeaderInfo && newItem instanceof HeaderInfo)
                {
                    return ((HeaderInfo) oldItem).title.equals(((HeaderInfo)newItem).title);
                }

                return false;
            }
        });
    }


    class FolderVH extends RecyclerView.ViewHolder
    {
        private final TextView title;

        public  FolderVH(View itemView)
        {
            super(itemView);
            title = itemView.findViewById(R.id.textView_dirsItemFolder_title);
        }
        public void bind(RecyclerView.ViewHolder holder, FolderInfo info)
        {
            title.setText(info.getTitle());
            holder.itemView.setOnClickListener(v->
            {
                if(fromDirsAdapter != null)
                {
                    fromDirsAdapter.accept(FromDirsAdapterEnum.CHANGE_FOLDER, info.getId());
                }
            });
        }
    }

    class NoteVH extends RecyclerView.ViewHolder
    {
        private final TextView title;
        public NoteVH(View itemView)
        {
            super(itemView);
            title = itemView.findViewById(R.id.textView_dirsItemNote_title);
        }
        public void bind(RecyclerView.ViewHolder holder, NoteInfo info)
        {
            title.setText(info.getTitle());
            holder.itemView.setOnClickListener(v->
            {
                // TODO : 노트 들어가기
            });
        }
    }

    class HeaderVH extends RecyclerView.ViewHolder
    {
        private final TextView title;
        public HeaderVH(View itemView)
        {
            super(itemView);
            title = itemView.findViewById(R.id.textView_dirsItemHeader);
        }
        public void bind(RecyclerView.ViewHolder holder, HeaderInfo info)
        {
            title.setText(info.getTitle());
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return getItem(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType)
        {
            case FileInfo.TYPE_FOLDER:
                return new FolderVH(inflater.inflate(R.layout.dirs_item_folder, parent,
                        false));
            case FileInfo.TYPE_NOTE:
                return new NoteVH(inflater.inflate(R.layout.dirs_item_note, parent,
                        false));
            case FileInfo.TYPE_HEADER:
                return new HeaderVH(inflater.inflate(R.layout.dirs_item_header, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        FileInfo item = getItem(position);

        switch(item.getType())
        {
            case NoteInfo.TYPE_FOLDER:
                ((FolderVH)holder).bind(holder, (FolderInfo)item);
                break;
            case NoteInfo.TYPE_NOTE:
                ((NoteVH)holder).bind(holder, (NoteInfo) item);
                break;
            case NoteInfo.TYPE_HEADER:
                ((HeaderVH)holder).bind(holder, (HeaderInfo) item);
                break;
        }
    }
}
