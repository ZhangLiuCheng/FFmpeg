package com.ylcq.ffmpeg;

public class FFEncoder {

    static {
        System.loadLibrary("ffencoder");
    }

    public native static int init(int width, int height);
    public native static byte[] encoding(byte[] yuvData);
    public native static void close();
}
