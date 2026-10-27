package com.example.stampit2.NoteView;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.example.stampit2.R;

import java.util.function.Consumer;

public class MoveButton extends androidx.appcompat.widget.AppCompatTextView
{
    private Consumer<View> onOutsideClicked;


    // drawable state 커스텀 추가
    private boolean bDragOutside = false;
    private static final int[] STATE_DRAG_OUTSIDE = {
            R.attr.state_drag_outside // res/values/attrs.xml 에 사용자 정의한 state_Drag_outside 와 STATE_DRAG_OUTSIDE 를 연동
    };
    @Override
    protected int[] onCreateDrawableState(int extraSpace) // setDragOutside 에서 호출됨
    {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if(bDragOutside)
        {
            mergeDrawableStates(drawableState, STATE_DRAG_OUTSIDE); // merge 로 xml 의 state 에 state_drag_outside 를 추가한다.
        }
        // 만약 bDragOutside 가 false 라면, merge 를 하지 않기 때문에 state_drag_outside == false 가 된다

        return drawableState;
    }
    protected void setDragOutside(boolean val)
    {
        if(bDragOutside != val)
        {
            bDragOutside = val;
            refreshDrawableState(); // onCreateDrawableState 를 호출
        }
    }


    public MoveButton(Context context)
    { super(context); }

    public MoveButton(Context context, android.util.AttributeSet attrs)
    { super(context, attrs); }

    public void setOnOff(boolean inBOn)
    {
        setEnabled(inBOn);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        setClickable(true);
        setFocusable(true);
        setBackgroundResource(R.drawable.move_button_selector);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                setDragOutside(false);
                break;
            case MotionEvent.ACTION_MOVE:
                if(isPressed())
                {
                    if(isHover(event))
                    {
                        setPressed(true);
                        setDragOutside(false);
                    }
                    else if(isbDragOutside(event))
                    {
                        setPressed(true);
                        setDragOutside(true);
                    }
                    else
                    {
                        setPressed(false);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if(isPressed())
                {
                    setPressed(false);

                    if(isEnabled() && isHover(event))
                    {
                        // setOnClickListener 에 등록된 함수를 호출
                        performClick();
                    }
                    else if(isbDragOutside(event))
                    {
                        if(onOutsideClicked != null)
                        {
                            onOutsideClicked.accept(this);
                        }
                    }
                }


                
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    private boolean isHover(MotionEvent event)
    {
        return event.getX() >= 0
                && event.getX() < getWidth()
                && event.getY() >= 0
                && event.getY() < getHeight();
    }
    private boolean isbDragOutside(MotionEvent event)
    {
        return event.getX() >= 0
                && event.getX() < getWidth()
                && event.getY() >= getHeight()
//                && event.getY() < getHeight() * 2
                ;
    }

    public void setOnOutsideClickedListener(Consumer<View> runnable)
    {
        onOutsideClicked = runnable;
    }
}
