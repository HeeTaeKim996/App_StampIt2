package com.example.stampit2.NoteView;


import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.stampit2.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class NoteInterface extends ConstraintLayout
{
    private Button insertImageButton;
    private ImageView noteImage;
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;


    private float minScale = 1.f;
    private float currScale = 1.f;

    private int noteImageWidth;
    private int noteImageHeight;

    public static interface OnAddPhotoClickListener
    {
        void onAddPhotoClicked();
    }
    private OnAddPhotoClickListener listener;


    public NoteInterface(Context context)
    { super(context); }

    public NoteInterface(Context context, @Nullable AttributeSet attrs)
    { super(context, attrs); }

    public NoteInterface(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    { super(context, attrs, defStyleAttr); }


    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        insertImageButton = findViewById(R.id.insertImageButton);
        noteImage = findViewById(R.id.noteImage);

        // 이미지 로드
        insertImageButton.setOnClickListener(view->
        {
            if(listener != null)
            {
                listener.onAddPhotoClicked();
            }
        });

        Context context = getContext();

        File imageFile = new File(context.getFilesDir(), "TempImage");

        if(imageFile.exists())
        {
            Uri savedUri = Uri.fromFile(imageFile);
            noteImage.setImageURI(savedUri);
        }


        // 줌인-줌아웃(스케일), 드래그(아핀 x,y ) 처리
        scaleGestureDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = detector.getScaleFactor();


                float targetScale = scale * currScale;
                targetScale = Math.max(minScale, Math.min(targetScale, minScale * 3.f));

                float adjustedScale = targetScale / currScale;
                currScale = targetScale;

                // postScale 의 1, 2 는 scale x, y 가 아니라 scale x, y 에 대한 *= 
                // 따라서 위처럼 adjustedScale 을 따로 구함
                matrix.postScale(adjustedScale, adjustedScale, detector.getFocusX(), detector.getFocusY());

                enforceBounds();

                noteImage.setImageMatrix(matrix);
                return true;
            }
        });

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener()
        {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
            {

                matrix.postTranslate(-distanceX, -distanceY);

                enforceBounds();

                noteImage.setImageMatrix(matrix);
                return true;
            }
        });

        
        noteImage.setOnTouchListener((v, event)->
        {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return true;
        });

        // noteImage 에 이미지가 불러와진 후 발동 (추후에는 필요 없을 것 같음)
        noteImage.post(()->
        {
            updateNoteImagesPerimeter();
            setMaxScalePosByNoteImage();
        });
    }

    // 기기가 가로모드 <-> 세로모드 전환 때 호출. 초기화 때도 호출되지만, 그 때는 noteImage == null
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        if(noteImage == null || noteImage.getDrawable() == null) return;

        updateNoteImagesPerimeter();
        setMaxScalePosByNoteImage();
    }


    public void setOnAddPhotoClicked(OnAddPhotoClickListener inListener)
    {
        listener = inListener;
    }

    public void setImage(Uri uri)
    {
        // 이미지를 저장
        try
        {
            Context context = getContext();
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            File imageFile = new File(context.getFilesDir(), "TempImage");

            FileOutputStream fos = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int len;
            while((len = inputStream.read(buffer)) > 0)
            {
                fos.write(buffer, 0, len);
            }

            fos.flush();
            fos.close();
            inputStream.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        
        // 이미지 할당
        noteImage.setImageURI(uri);


        updateNoteImagesPerimeter();
        setMaxScalePosByNoteImage();
    }

    private void setMaxScalePosByNoteImage()
    {
        // Set Scale
        int intrinsicWidth = noteImage.getDrawable().getIntrinsicWidth();
        int intrinsicHeight = noteImage.getDrawable().getIntrinsicHeight();

        float xRatio = (float) noteImageWidth / intrinsicWidth;
        float yRatio = (float) noteImageHeight / intrinsicHeight;

        minScale = Math.min(yRatio, xRatio);

        currScale = minScale;
        matrix.setScale(currScale, currScale);

        // Set Translation
        float xPos = (noteImageWidth - intrinsicWidth * currScale) / 2.f;
        float yPos = (noteImageHeight - intrinsicHeight * currScale) / 2.f;

        matrix.postTranslate(xPos, yPos);

        noteImage.setImageMatrix(matrix);
    }


    private void updateNoteImagesPerimeter()
    {
        noteImageWidth = noteImage.getWidth();
        noteImageHeight = noteImage.getHeight();
    }

    private void enforceBounds()
    {
        // 공용변수
        boolean changed = false;
        float[] metValues = new float[9];
        matrix.getValues(metValues);
        float curr, min, max, scaledIntrinsic;

        // X
        scaledIntrinsic = currScale * noteImage.getDrawable().getIntrinsicWidth();
        if(noteImageWidth > scaledIntrinsic)
        // 사진이 화면 안에서 이동
        {
            min = 0;                                  // 사진이 화면의 왼쪽에 붙음
            max = noteImageWidth - scaledIntrinsic;   // 사진이 화면의 오른쪽에 붙음
        }
        else
        // 화면이 사진 안에서 이동
        {
            min = noteImageWidth - scaledIntrinsic;   // 화면이 사진의 오른쪽에 붙음
            max = 0;                                  // 화면이 사진의 왼쪽에 붙음
        }
        curr = metValues[Matrix.MTRANS_X];
        float moveX = 0;
        if(curr < min)
        {
            changed = true;
            moveX = min - curr;
        }
        else if(curr > max)
        {
            changed = true;
            moveX = max - curr;
        }


        // Y ( X 와 대칭 )
        scaledIntrinsic = currScale * noteImage.getDrawable().getIntrinsicHeight();
        if(noteImageHeight > scaledIntrinsic)
        {
            min = 0;
            max = noteImageHeight - scaledIntrinsic;
        }
        else
        {
            min = noteImageHeight - scaledIntrinsic;
            max = 0;
        }
        curr = metValues[Matrix.MTRANS_Y];
        float moveY = 0;
        if(curr < min)
        {
            changed = true;
            moveY = min - curr;
        }
        else if(curr > max)
        {
            changed = true;
            moveY = max - curr;
        }


        if(changed)
        {
            matrix.postTranslate(moveX, moveY);
        }
    }

}
