package com.ylcq.ffmpeg;

public class FFPusher {

    static {
        System.loadLibrary("FFPusher");
    }

    public native static int getVersion();
    public native static int encoding(byte[] data);
    public native static int close();
}
