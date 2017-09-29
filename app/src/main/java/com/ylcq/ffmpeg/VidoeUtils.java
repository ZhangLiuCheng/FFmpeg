package com.ylcq.ffmpeg;

public class VidoeUtils {

    static {
        System.loadLibrary("Videoutils");
    }

    /**
     * 将src视频解码成yuv视频.
     * @param src
     * @param dest
     */
    public native static void decode(String src, String dest);
}
