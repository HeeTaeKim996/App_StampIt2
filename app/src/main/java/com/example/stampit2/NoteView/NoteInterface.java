package com.example.stampit2.NoteView;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
    private MoveButton toNextButton;
    private MoveButton toBeforeButton;
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
        toBeforeButton = findViewById(R.id.button_toBefore);
        noteImage = findViewById(R.id.noteImage);

        toNextButton.setOnClickListener(v-> { onToNextButtonClicked(); });
        toNextButton.setOnOutsideClickedListener(v-> { InformUtils.instance().showOnlyInfo(getContext(), "아웃 체크"); });

        toBeforeButton.setOnClickListener(v->{ onToBeforeButtonClicked(); });
        toBeforeButton.setOnOutsideClickedListener(v-> { InformUtils.instance().showOnlyInfo(getContext(), "아웃 체크"); });

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

            Uri imgUri = loadNoteByPage(getNotePageFile(noteInfo.noteFile, (short)1));
            if(imgUri != null)
            {
                loadNoteImage(imgUri);
                
                // 이미지 로드 성공시, loadNoteImage 의 Glide 에서 비동기로 스케일, 위치 조정하니, 여기서 종료
                return;
            }

        }




        // 이미지 로드 실패시, 호출
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

                    // TODO : page 생성시 초기화 작업 처리
                    File newFile = getNotePageFile(noteInfo.noteFile, (short) (noteInfo.currPage));
                    newFile.mkdir();

                    saveNoteByPage(newFile, uri);
                }

                catch(IOException e)
                {
                    e.printStackTrace();
                }

            }
        }

        // 갱신된 데이터 저장
        noteInfo.pages++;
        saveNoteInfo();

        // 이미지 할당
        loadNoteImage(uri);
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
            noteInfo.currPage++;

            Uri imgUri = loadNoteByPage(getNotePageFile(noteInfo.noteFile, noteInfo.currPage));

            loadNoteImage(imgUri);
        }
    }

    private void onToBeforeButtonClicked()
    {
        if(noteInfo.currPage > 1)
        {
            noteInfo.currPage--;

            Uri imgUri = loadNoteByPage(getNotePageFile(noteInfo.noteFile, noteInfo.currPage));

            loadNoteImage(imgUri);
        }
    }



    private File getNoteVersionFile(File notePageFile, short version)
    {
        return new File(notePageFile + "/vs", String.valueOf(version));
    }
    private void saveNoteByPage(File noteFile, Uri uri) throws IOException
    {
        // TODO : 버전에 대한 메타 데이터 생성

        Context context = getContext();

        // 처음 Page에 대한 저장이므로, 버전은 무조건 1
        File versionFile = getNoteVersionFile(noteFile, (short)1);
        versionFile.mkdirs();
        File imageFile = new File(versionFile, "img");


        InputStream inputStream = context.getContentResolver().openInputStream(uri);
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
    private Uri loadNoteByPage(File pageFile)
    {
        // TODO : 버전에 대한 메타 데이터에 기반하여 마지막 버전의 img 를 리턴
        
        Context context = getContext();

        File versionFile = new File(getNoteVersionFile(pageFile, (short)1), "img");

        if(versionFile.exists() == false)
        {
            InformUtils.instance().showOnlyInfo(context, 
                    "NoteInterface : 이미지가 버전 파일이 없거나 버전 파일 내에 img 가 없음");
            return null;
        }
        return Uri.fromFile(versionFile);
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

}
