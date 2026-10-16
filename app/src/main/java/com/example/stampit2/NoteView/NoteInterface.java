package com.example.stampit2.NoteView;


import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.AttributeSet;
import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.stampit2.R;
import com.example.stampit2.Utils.FileUtils;
import com.example.stampit2.Utils.InformUtils;
import com.example.stampit2.databinding.NotePageConfigBinding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Consumer;


public class NoteInterface extends ConstraintLayout
{
    private MoveButton toNextButton;
    private MoveButton toBeforeButton;

    private ImageView noteImage;

    private TextView pageInfo;

    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;


    private float minScale = 1.f;
    private float currScale = 1.f;

    private int noteImageWidth;
    private int noteImageHeight;

    private NoteDataAdmin dataAdmin;
    private Short currPage;

    private enum NoteImageState
    {
        NOT_ASSIGNED,
        ADD_MODE,
        DRAG_MODE
    }
    private NoteImageState noteImageState = NoteImageState.NOT_ASSIGNED;

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

        toNextButton = findViewById(R.id.button_note_toNext);
        toBeforeButton = findViewById(R.id.button_note_toBefore);
        noteImage = findViewById(R.id.imageview_note_noteImage);
        pageInfo = findViewById(R.id.textView_note_pageInfo);

        setPageConfigButton();

        toNextButton.setOnClickListener(v-> { onToNextButtonClicked(); });
        toNextButton.setOnOutsideClickedListener(v-> { onInsertButtonClicked(true); });

        toBeforeButton.setOnClickListener(v->{ onToBeforeButtonClicked(); });
        toBeforeButton.setOnOutsideClickedListener(v-> { onInsertButtonClicked(false); });
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


    public void initializeNote(String rootPath, boolean isCreating)
    {
        dataAdmin = new NoteDataAdmin(getContext(), rootPath, isCreating);

        currPage = dataAdmin.getPageSize() > 0 ? (short) 0 : -1;

        updateInterfaces();
    }


    public void setOnAddPhotoClicked(OnAddPhotoClickListener inListener)
    {
        listener = inListener;
    }

    public void insertPage(Uri uri, boolean isNext)
    {
        currPage = dataAdmin.insertPage(uri, currPage, isNext);
        updateInterfaces();
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





    private void onToNextButtonClicked()
    {
        currPage++;
        updateInterfaces();
    }

    private void onToBeforeButtonClicked()
    {
        currPage--;
        updateInterfaces();
    }

    private void loadImageByCurrPage()
    {
        Uri imgUri = dataAdmin.loadNoteByPage(currPage);
        loadNoteImage(imgUri);
    }


    private void onInsertButtonClicked(boolean isNext)
    {
        if(listener != null)
        {
            listener.onAddPhotoClicked(isNext);
        }
    }




    private void loadNoteImage(Uri uri)
    {
//        noteImage.setImageURI(uri);

        // ■ Glide
        // ImageView.setImageURI 는 동기로 데이터를 비트맵에 온전히 로드한다. 카메라 해상도는 굉장히 높기에,
        // setImageURI 로 로드시 해상도가 높은 사진을 로드시 시간이 오래 걸린다.
        // Glide 는 외부 라이브러리로, 사진을 ImageView 의 크기에 맞춰 적당한 해상도로 로드하며,
        // 비동기로 로드한다.
        //
        // 아래의 onResourceReady 는 비동기로 사진이 로드된 후에 호출된다.
        //
        // ※ Glide 를 사용하려면 build.gradle 에 glide 사용 코드를 추가해야 한다
        
        Glide.with(getContext().getApplicationContext())
                .load(uri)
                .listener(new RequestListener<Drawable>()
                {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource)
                    {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource)
                    {
                        // 이미지가 비동기로 완전히 로드된 후에, 스케일과 위치를 설정
                        noteImage.post(()->
                        {
                            updateNoteImagesPerimeter();
                            setMinScalePosByNoteImage();
                        });

                        return false;
                    }
                })
                .into(noteImage);
    }

    private void updateInterfaces()
    {
        updateNoteImageInterface();

        if(currPage <= 0)
        {
            toBeforeButton.setOnOff(false);
        }
        else
        {
            toBeforeButton.setOnOff(true);
        }

        if(currPage == dataAdmin.getPageSize() - 1)
        {
            toNextButton.setOnOff(false);
        }
        else
        {
            toNextButton.setOnOff(true);
        }

        pageInfo.setText((currPage + 1) + " / " + (dataAdmin.getPageSize()));
    }



    private void updateNoteImageInterface()
    {
        Context context = getContext();

        if(dataAdmin.getPageSize() > 0)
        // 드래그가 가능하도록 처리
        {
            loadImageByCurrPage();

            if(noteImageState != NoteImageState.DRAG_MODE)
            {
                noteImage.setOnClickListener(null);


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

                noteImageState = NoteImageState.DRAG_MODE;
            }


        }
        else
        // 터치시 추가 기능 처리
        {
            if(noteImageState != NoteImageState.ADD_MODE)
            {
                noteImage.setImageResource(android.R.drawable.btn_plus);


                noteImage.setOnTouchListener(null);
                noteImage.setOnClickListener(v->
                {
                    onInsertButtonClicked(true);
                });


                noteImage.post(()->
                {
                    updateNoteImagesPerimeter();
                    setMinScalePosByNoteImage();
                });

                noteImageState = NoteImageState.ADD_MODE;
            }
        }

    }


    private void setPageConfigButton()
    {
        Context context = getContext();

        Button pageConfigButton = findViewById(R.id.button_note_pageConfig);

        pageConfigButton.setOnClickListener(v->
        {
            NotePageConfigBinding binding = NotePageConfigBinding
                    .inflate(LayoutInflater.from(context));
            AlertDialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot())
                    .create();

            binding.buttonNotePageConfigDeletePage.setOnClickListener(v1->
            {
                onPageDeleteButtonClicked();
                dialog.dismiss();
            });

            binding.buttonNotePageConfigExit.setOnClickListener(v2->
            {
                dialog.dismiss();
            });

            binding.buttonNotePageConfigForDebugListAllFiles.setOnClickListener(v3->
            {
                InformUtils.instance().showOnlyInfo(context,
                        FileUtils.instance().getAllFiles(context).toString());
                dialog.dismiss();
            });

            dialog.show();
        });
    }

    private void onPageDeleteButtonClicked()
    {
        if(currPage == -1) return;

        Consumer<Boolean> checkYes = (Boolean bYes)->
        {
            if(bYes)
            {
                dataAdmin.deletePage(currPage);

                if(--currPage == -1 && dataAdmin.getPageSize() > 0)
                {
                    currPage = 0;
                }

                updateInterfaces();
            }
        };

        InformUtils.instance().showYesOrNo(getContext(), "현 페이지를 삭제하겠습니까?", checkYes);
    }


}
