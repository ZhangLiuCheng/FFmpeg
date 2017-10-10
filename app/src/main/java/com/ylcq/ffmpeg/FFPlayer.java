package com.ylcq.ffmpeg;


import android.view.Surface;

public class FFPlayer {

    static {
        System.loadLibrary("FFPlayer");
    }

    public native static void render(String videopath, Surface surface);
}
