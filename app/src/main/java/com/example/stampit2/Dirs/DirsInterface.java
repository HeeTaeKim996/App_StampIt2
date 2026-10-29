package com.example.stampit2.Dirs;

import android.content.Context;
import android.provider.CalendarContract;
import android.util.AttributeSet;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stampit2.R;
import com.example.stampit2.Utils.InformUtils;

import java.io.File;
import java.util.Calendar;

public class DirsInterface extends ConstraintLayout
{

    private Button dirSummaryButton;
    private Button insertNoteButton;
    private Button othersButton;
    private Button searchButton;




    public DirsInterface(Context context)
    { super(context); init(context);}

    public DirsInterface(Context context, AttributeSet attrs)
    { super(context, attrs); init(context);}

    public DirsInterface(Context context, AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); init(context);}


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        dirSummaryButton = findViewById(R.id.button_dirs_dirSummary);
        insertNoteButton = findViewById(R.id.button_dirs_insertNote);
        othersButton = findViewById(R.id.button_dirs_others);
        searchButton = findViewById(R.id.button_dirs_searchNote);

    }

    private void init(Context context)
    {
        InformUtils.instance().showOnlyInfo(getContext(),
                getTimeInfoFromNoteId(getNoteIdFromNow()));
    }


    private int getNoteIdFromNow()
    {
        Calendar c = Calendar.getInstance();
        int id = (c.get(Calendar.YEAR) - 2000 << 23) | (c.get(Calendar.MONTH) + 1 << 19) |
                (c.get(Calendar.DATE) << 14) | (c.get(Calendar.HOUR_OF_DAY) << 9) |
                (c.get(Calendar.MINUTE) << 3) | (c.get(Calendar.MILLISECOND) & 7);
        
        // TODO : id 충돌시, ++ 를 반복해가며 충돌 회피

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

        return String.format("%04d-%02d-%02d(%02d:%02d-%01d)",
                year + 2000, month, date, hour, minute, milli);
    }
}
