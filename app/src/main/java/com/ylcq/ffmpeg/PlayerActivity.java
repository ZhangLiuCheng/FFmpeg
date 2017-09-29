package com.ylcq.ffmpeg;

import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {

    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
    }

    public void play(View view) {

//        final String videopath = "/sdcard/video/a.mp4";
//        final String videopath = "/sdcard/video/720pq.h264";
        final String videopath = "/storage/emulated/0/Android/data/com.ylcq.ffmpeg/cache/camera.h264";

//        File rootFile = getExternalCacheDir();
//        final String videopath = rootFile.getAbsolutePath() + "/zhang.h264";

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
        final Surface surface = surfaceHolder.getSurface();

        new Thread(new Runnable() {
            @Override
            public void run() {
                FFPlayer.render(videopath, surface);
            }
        }).start();
    }
}
