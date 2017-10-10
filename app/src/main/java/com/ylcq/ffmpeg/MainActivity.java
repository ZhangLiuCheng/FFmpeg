package com.ylcq.ffmpeg;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void yuv(View view) {
        File rootFile = getExternalCacheDir();
        String src = "/sdcard/video/a.mp4";
        String dest = rootFile.getAbsolutePath() + "/abc.yuv";
        Log.v("Qingyuan", dest);
        VidoeUtils.decode(src, dest);
    }

    public void push(View view) {
        Intent intent = new Intent(this, PusherActivity.class);
        startActivity(intent);
    }

    public void play(View view) {
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }


    public void encoder(View view) {
        Intent intent = new Intent(this, EncoderActivity.class);
        startActivity(intent);
    }

    public void decoder(View view) {
        Intent intent = new Intent(this, DecoderActivity.class);
        startActivity(intent);
    }
}
