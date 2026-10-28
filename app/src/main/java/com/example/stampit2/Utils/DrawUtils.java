package com.example.stampit2.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class DrawUtils
{
    private static DrawUtils _instance;

    public static DrawUtils instance()
    {
        if(_instance == null)
        {
            _instance = new DrawUtils();
        }
        return _instance;
    }


    private  DrawUtils()
    { }



    // ■ Glide
    // ImageView.setImageURI 는 동기로 데이터를 비트맵에 온전히 로드한다. 카메라 해상도는 굉장히 높기에,
    // setImageURI 로 로드시 해상도가 높은 사진을 로드시 시간이 오래 걸린다.
    // Glide 는 외부 라이브러리로, 사진을 ImageView 의 크기에 맞춰 적당한 해상도로 로드하며,
    // 비동기로 로드한다.
    //
    // 아래의 onResourceReady 는 비동기로 사진이 로드된 후에 호출된다.
    //
    // ※ Glide 를 사용하려면 build.gradle 에 glide 사용 코드를 추가해야 한다
    public void drawByGlide(Context context, ImageView imageView, Uri uri,
                               Runnable onResourceReadyFunc)
    {
        Glide.with(context.getApplicationContext())
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

                        imageView.post(()->
                        {
                            if(onResourceReadyFunc != null)
                            {
                                onResourceReadyFunc.run();
                            }
                        });
                        return false;
                    }
                })
                .into(imageView);


    }

}
