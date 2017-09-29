package com.ylcq.ffmpeg;


import android.view.Surface;

public class FFPlayer {

    static {
        System.loadLibrary("ffplayer");
    }

    public native static void render(String videopath, Surface surface);
}
