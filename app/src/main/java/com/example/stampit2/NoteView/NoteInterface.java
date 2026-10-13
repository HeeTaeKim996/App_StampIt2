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
import com.example.stampit2.Utils.InformUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class NoteInterface extends ConstraintLayout
{
    private Button toNextButton;
    private ImageView noteImage;
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;


    private float minScale = 1.f;
    private float currScale = 1.f;

    private int noteImageWidth;
    private int noteImageHeight;

    private NoteInfo noteInfo;


    public static interface OnAddPhotoClickListener
    {
        void onAddPhotoClicked(boolean isNext);
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

        toNextButton = findViewById(R.id.button_toNext);
        noteImage = findViewById(R.id.noteImage);

        toNextButton.setOnClickListener(view-> { onToNextButtonClicked(); });

        Context context = getContext();

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
    }

    // 기기가 가로모드 <-> 세로모드 전환 때 호출. 초기화 때도 호출되지만, 그 때는 noteImage == null
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        if(noteImage == null || noteImage.getDrawable() == null) return;

        updateNoteImagesPerimeter();
        setMinScalePosByNoteImage();
    }


    public void initializeNote(File noteInfoFile)
    {

        if(noteInfoFile.exists() == false)
        {
            createNoteFile(noteInfoFile);
        }
        else
        {
            loadFromNoteFile(noteInfoFile);
        }


        Context context = getContext();

        // 이미지 로드
        if(noteInfo.pages > 0)
        {
            noteInfo.currPage = 1;
            File imageFile = new File(getNotePageFile(noteInfo.noteFile, (short) 1), "Temp");
            if(imageFile.exists())
            {
                Uri savedUri = Uri.fromFile(imageFile);
                noteImage.setImageURI(savedUri);
            }
            else
            {
                InformUtils.instance().showOnlyInfo(context, "NoteInterface : 이미지가 없음");
            }
        }




        // noteImage 에 이미지가 불러와진 후 발동
        noteImage.post(()->
        {
            updateNoteImagesPerimeter();
            setMinScalePosByNoteImage();
        });
    }


    public void setOnAddPhotoClicked(OnAddPhotoClickListener inListener)
    {
        listener = inListener;
    }

    public void insertPage(Uri uri, boolean isNext)
    {
        Context context = getContext();

        if(isNext)
        {
            if(noteInfo.currPage == noteInfo.pages)
            {
                noteInfo.currPage++;

                try
                {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);

                    File newFile = getNotePageFile(noteInfo.noteFile, (short) (noteInfo.currPage));
                    newFile.mkdir();
                    File imageFile = new File(newFile, "Temp");

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
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }

        // 갱신된 데이터 저장
        noteInfo.pages++;
        saveNoteInfo();

        // 이미지 할당
        noteImage.setImageURI(uri);


        updateNoteImagesPerimeter();
        setMinScalePosByNoteImage();
    }

    private void setMinScalePosByNoteImage()
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



    private File getNoteInfoMetaFile(File noteFile)
    {
        return new File(noteFile, "NoteInfo.meta");
    }
    private void createNoteFile(File noteFile)
    {
        noteInfo = new NoteInfo();
        noteInfo.noteFile = noteFile;
        noteInfo.pages = 0;
        noteInfo.currPage = 0;

        // TODO : 추후 디렉터리를 meta로 연계시, 파일을 만들 때 meta 에 폴더 추가 내용을 갱신해야 함

        new File(noteFile, "pages").mkdirs();

        saveNoteInfo();
    }
    private void loadFromNoteFile(File noteFile)
    {
        noteInfo = new NoteInfo();
        noteInfo.noteFile = noteFile;

        try(DataInputStream dis = new DataInputStream(new FileInputStream(getNoteInfoMetaFile(noteFile))))
        {
            noteInfo.pages = dis.readShort(); // pages
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }
    private void saveNoteInfo()
    {
        try(DataOutputStream dos
                    = new DataOutputStream(new FileOutputStream(getNoteInfoMetaFile(noteInfo.noteFile))))
        {
            dos.writeShort(noteInfo.pages); // pages
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }





    private File getNotePageFile(File noteFile, short page)
    {
        return new File(noteFile + "/pages", String.valueOf(page));
    }

    private void onToNextButtonClicked()
    {
        if(noteInfo.currPage == noteInfo.pages)
        {
            if(listener != null)
            {
                listener.onAddPhotoClicked(true);
            }
        }
        else
        {
            // TODO : 다음 페이지로 이동하기
        }
    }
}
