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

        toNextButton = findViewById(R.id.button_note_toNext);
        toBeforeButton = findViewById(R.id.button_note_toBefore);
        noteImage = findViewById(R.id.imageview_note_noteImage);
        pageInfo = findViewById(R.id.textView_note_pageInfo);

        setPageConfigButton();

        toNextButton.setOnClickListener(v-> { onToNextButtonClicked(); });
        toNextButton.setOnOutsideClickedListener(v-> { onInsertButtonClicked(true); });

        toBeforeButton.setOnClickListener(v->{ onToBeforeButtonClicked(); });
        toBeforeButton.setOnOutsideClickedListener(v-> { onInsertButtonClicked(false); });

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



        updateInterfaces();



        Context context = getContext();

        // 이미지 로드
        if(noteInfo.pages.size() > 0)
        {
            loadImageByCurrPage();
            return;
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

        noteInfo.currPage = noteInfo.pages.size() == 0 ? (short) 0
                : (short) (isNext ? noteInfo.currPage + 1 : noteInfo.currPage);

        try
        {
            // TODO : page 생성시 초기화 작업 처리
            File newFile = getNotePageFile(noteInfo.noteFile, noteInfo.lastId);
            newFile.mkdir();

            saveNoteByPage(newFile, uri);
        }

        catch(IOException e)
        {
            e.printStackTrace();
            InformUtils.instance().showOnlyInfo(context, "오류 : NoteInterface - insertPage");
            return;
        }


        noteInfo.pages.add(noteInfo.currPage, noteInfo.lastId++);
        saveNoteInfo();

        loadImageByCurrPage();
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



    private File getNoteInfoMetaFile(File noteFile)
    {
        return new File(noteFile, "NoteInfo.meta");
    }
    private void createNoteFile(File noteFile)
    {
        noteInfo = new NoteInfo();
        noteInfo.noteFile = noteFile;
        noteInfo.pages = new ArrayList<Short>();
        noteInfo.currPage = -1;
        noteInfo.lastId = 0;

        new File(noteFile, "p").mkdirs();
        saveNoteInfo();
    }


    private void loadFromNoteFile(File noteFile)
    {
        noteInfo = new NoteInfo();
        noteInfo.noteFile = noteFile;

        try(DataInputStream dis = new DataInputStream(new FileInputStream(getNoteInfoMetaFile(noteFile))))
        {
            noteInfo.lastId = dis.readShort();

            int pageSize = dis.readInt();
            noteInfo.pages = new ArrayList<>(pageSize);
            while(pageSize-- > 0)
            {
                noteInfo.pages.add(dis.readShort());
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            InformUtils.instance().showOnlyInfo(getContext(), "오류 : NoteInterface - loadFromNoteFile");
        }

        noteInfo.currPage = noteInfo.pages.size() > 0 ? (short) 0 : -1;

    }
    private void saveNoteInfo()
    {
        try(DataOutputStream dos
                    = new DataOutputStream(new FileOutputStream(getNoteInfoMetaFile(noteInfo.noteFile))))
        {
            dos.writeShort(noteInfo.lastId);

            dos.writeInt(noteInfo.pages.size());
            for(short id : noteInfo.pages)
            {
                dos.writeShort(id);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }





    private File getNotePageFileByPage(File noteFile, int page)
    {
        return getNotePageFile(noteFile, noteInfo.pages.get(page));
    }
    private File getNotePageFile(File noteFile, short pageFileName)
    {
        return new File(noteFile + "/p", String.valueOf(pageFileName));
    }

    private void onToNextButtonClicked()
    {

        noteInfo.currPage++;

        loadImageByCurrPage();

        updateInterfaces();
    }

    private void onToBeforeButtonClicked()
    {
        noteInfo.currPage--;

        loadImageByCurrPage();

        updateInterfaces();
    }

    private void loadImageByCurrPage()
    {
        Uri imgUri = loadNoteByPage(getNotePageFileByPage(noteInfo.noteFile, noteInfo.currPage));
        loadNoteImage(imgUri);
    }


    private void onInsertButtonClicked(boolean isNext)
    {
        if(listener != null)
        {
            listener.onAddPhotoClicked(isNext);
        }
    }



    private File getNoteVersionFile(File notePageFile, short version)
    {
        return new File(notePageFile + "/v", String.valueOf(version));
    }
    private void saveNoteByPage(File noteFile, Uri uri) throws IOException
    {
        // TODO : 버전에 대한 메타 데이터 생성

        Context context = getContext();

        // 처음 Page에 대한 저장이므로, 버전은 무조건 1
        File versionFile = getNoteVersionFile(noteFile, (short)1);
        versionFile.mkdirs();
        File imageFile = new File(versionFile, getOriginName(uri) + ".img");


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

        File versionFolder = getNoteVersionFile(pageFile, (short)1);

        if (versionFolder.exists() == false || versionFolder.isDirectory() == false)
        {
            InformUtils.instance().showOnlyInfo(context,
                    "NoteInterface : 버전 폴더가 존재하지 않음");
            return null;
        }

        // 폴더 내에서 .img로 끝나는 파일을 검색
        File[] imageFiles = versionFolder.listFiles((dir, name) -> name.endsWith(".img"));

        if (imageFiles == null || imageFiles.length == 0)
        {
            InformUtils.instance().showOnlyInfo(context,
                    "NoteInterface : 버전 파일 내에 img 가 없음");
            return null;
        }

        // 저장된 첫 번째 이미지 파일을 리턴
        return Uri.fromFile(imageFiles[0]);
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
        if(noteInfo.currPage <= 0)
        {
            toBeforeButton.setOnOff(false);
        }
        else
        {
            toBeforeButton.setOnOff(true);
        }

        if(noteInfo.currPage == noteInfo.pages.size() - 1)
        {
            toNextButton.setOnOff(false);
        }
        else
        {
            toNextButton.setOnOff(true);
        }

        pageInfo.setText(String.valueOf(noteInfo.currPage + 1) +
                " / " + String.valueOf(noteInfo.pages.size()));
    }




    private String getOriginName(Uri uri)
    {
        try (Cursor cursor = getContext().getContentResolver().query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null))
        {
            if (cursor != null && cursor.moveToFirst())
            {
                return cursor.getString(
                        cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "unknown";
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


            dialog.show();
        });
    }

    private void onPageDeleteButtonClicked()
    {
        if(noteInfo.currPage == -1) return;

        Consumer<Boolean> checkYes = (Boolean bYes)->
        {
            if(bYes)
            {
                File deleteFile = getNotePageFileByPage(noteInfo.noteFile, noteInfo.currPage);
                FileUtils.instance().deleteFile(deleteFile);

                noteInfo.pages.remove(noteInfo.currPage);
                noteInfo.currPage--;

                saveNoteInfo();

                loadImageByCurrPage();
                updateInterfaces();
            }
        };

        InformUtils.instance().showYesOrNo(getContext(), "현 페이지를 삭제하겠습니까?", checkYes);


    }
}
