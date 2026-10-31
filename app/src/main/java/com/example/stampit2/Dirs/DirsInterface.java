package com.example.stampit2.Dirs;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stampit2.Dirs.DB.DbRepository;
import com.example.stampit2.Note.NoteInterface;
import com.example.stampit2.R;
import com.example.stampit2.Utils.InformUtils;
import com.example.stampit2.databinding.DirsOtherSetBinding;
import com.example.stampit2.databinding.NoteBinding;
import com.example.stampit2.databinding.UtilsSetTextBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.BiConsumer;

public class DirsInterface extends ConstraintLayout
{



    private RecyclerView recyclerView;

    private List<Long> currFolders = new ArrayList<>();

    private File rootFile;
    private File getNoteFile(int id)
    {
        return new File(rootFile, "/n/" + String.valueOf(id));
    }
    private String getNoteFileToString(int id)
    {
        return rootFile.toString() + "/n/" + String.valueOf(id);
    }


    public DirsInterface(Context context)
    { super(context); }

    public DirsInterface(Context context, AttributeSet attrs)
    { super(context, attrs); }

    public DirsInterface(Context context, AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr);}


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();


        recyclerView = findViewById(R.id.recyclerView_dirs);

        init(getContext());
    }

    private void init(Context context)
    {
        rootFile = context.getFilesDir();

        DirsAdapter adapter = new DirsAdapter();
        adapter.setFromDirsAdapter(fromDirsAdapter);

        recyclerView.setAdapter(adapter);

        GridLayoutManager lm = new GridLayoutManager(context, 12);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {
            @Override
            public int getSpanSize(int position)
            {
                switch(adapter.getItemViewType(position))
                {
                    case FileInfo.TYPE_FOLDER:
                        return 3;
                    case FileInfo.TYPE_NOTE:
                        return 4;
                    case FileInfo.TYPE_HEADER:
                        return 12;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(lm);
        showCurrFiles();


        findViewById(R.id.button_dirs_otherSet).setOnClickListener(v-> { showOtherSetPanel(); });
        findViewById(R.id.button_dirs_toRootFolder).setOnClickListener(v-> { onToRootFolderButtonClicked(); });
        findViewById(R.id.button_dirs_addNote).setOnClickListener(v->{onAddNoteButtonClicked();});
    }


    private int getNoteIdFromNow()
    {
        Calendar c = Calendar.getInstance();
        int id = (c.get(Calendar.YEAR) - 2000 << 23) | (c.get(Calendar.MONTH) + 1 << 19) |
                (c.get(Calendar.DATE) << 14) | (c.get(Calendar.HOUR_OF_DAY) << 9) |
                (c.get(Calendar.MINUTE) << 3) | (c.get(Calendar.SECOND) / 10);

        while(getNoteFile(id).exists())
        {
            id++;
        }

        return id;
    }
    private String getTimeInfoFromNoteId(int noteId)
    {
        int year = noteId >> 23 & 255;
        int month = noteId >> 19 & 15;
        int date = noteId >> 14 & 31;
        int hour = noteId >> 9 & 31;
        int minute = noteId >> 3 & 63;
        int milli = noteId & 7;

        return String.format("%3d/%02d/%02d(%02d:%02d:%1d)",
                year, month, date, hour, minute, milli);
    }

    private void showOtherSetPanel()
    {
        Context context = getContext();
        DirsOtherSetBinding binding = DirsOtherSetBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        binding.buttonDirsOtherSetSelect.setOnClickListener(v->
        {
            InformUtils.instance().showOnlyInfo(context,"추가 예정");
        });
        binding.buttonDirsOtherSetSortSet.setOnClickListener(v->
        {
            InformUtils.instance().showOnlyInfo(context,"추가 예정");
        });
        binding.buttonDirsOtherSetViewSet.setOnClickListener(v->
        {
            InformUtils.instance().showOnlyInfo(context,"추가 예정");
        });

        binding.buttonDirsOtherSetAddFolder.setOnClickListener(v->
        {
            dialog.dismiss();
            onAddFolderButtonClicked();
        });


        dialog.show();
    }

    private void onAddFolderButtonClicked()
    {
        Context context = getContext();
        UtilsSetTextBinding binding = UtilsSetTextBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();

        binding.textViewUtilsSetText.setText("폴더 추가");
        binding.editTextUtilsSetText.setText("폴더1");

        binding.buttonUtilsSetTextCancel.setOnClickListener(v->
        {
            dialog.dismiss();
        });
        binding.buttonUtilsSetTextOk.setOnClickListener(v->
        {
            String folderName = binding.editTextUtilsSetText.getText().toString();
            if(folderName.equals(""))
            {
                InformUtils.instance().showOnlyInfo(context, "폴더 이름을 입력해주세요");

            }
            else
            {
                DbRepository.instance().insertFolder(getCurrFolder(), folderName);
            }

            showCurrFiles();

            dialog.dismiss();
        });


        dialog.show();
    }

    private long getCurrFolder()
    {
        return currFolders.isEmpty() ? 0 : currFolders.get(currFolders.size() - 1);
    }

    private void showCurrFiles()
    {
        DirsAdapter adapter = (DirsAdapter) recyclerView.getAdapter();
        adapter.submitList(DbRepository.instance().getFilesInFolder(getCurrFolder()));
    }


    public BiConsumer<FromDirsAdapterEnum, Long> fromDirsAdapter =(fromEnum, id)->
    {
        switch(fromEnum)
        {
            case CHANGE_FOLDER:
                long parentId = DbRepository.instance().getFoldersParentId(id);
                if(getCurrFolder() == parentId)
                {
                    currFolders.add(id);
                }
                else
                {

                    currFolders.clear();
                    currFolders.add(id);
                    while(parentId != 0)
                    {
                        currFolders.add(parentId);
                        parentId = DbRepository.instance().getFoldersParentId(parentId);
                    }

                    Collections.reverse(currFolders);
                }


                showCurrFiles();
                break;
        }
    };

    private void onToRootFolderButtonClicked()
    {
        // TODO : 나중에 root 폴더로. 지금은 임시로 부모 폴더로 이동으로 처리함

        if(currFolders.isEmpty() == false)
        {
            currFolders.remove(currFolders.size() - 1);
        }
        showCurrFiles();
    }

    private void onAddNoteButtonClicked()
    {
        int noteId = getNoteIdFromNow();
        Context context = getContext();


        if(DbRepository.instance().addNote(noteId, getCurrFolder()) == false)
        {
            InformUtils.instance().showOnlyInfo(getContext(), "노트생성실패");
            return;
        }

        String noteFileString = getNoteFileToString(noteId);

        NoteBinding binding = NoteBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                .create();

        dialog.show();

        NoteInterface noteInterface = (NoteInterface) binding.getRoot();
        noteInterface.initializeNote(noteFileString, true);
    }
}
