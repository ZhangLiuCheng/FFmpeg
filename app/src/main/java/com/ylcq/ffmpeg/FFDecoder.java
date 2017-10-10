package com.ylcq.ffmpeg;

import android.view.Surface;

public class FFDecoder {

    static {
        System.loadLibrary("ffdecoder");
    }

    public native static int init(int width, int height, Surface surface);
    public native static byte[] decoding(byte[] data);
    public native static void close();
}
