package com.ylcq.ffmpeg;

import android.graphics.PixelFormat;
import android.media.MediaCodec;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DecoderActivity extends AppCompatActivity {

    private final static String TAG = "H264Client";

    private SurfaceView surfaceView;
    private Surface surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoder);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
        surface = surfaceHolder.getSurface();

//        FFDecoder.init(864, 480, surface);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FFDecoder.close();
    }

    public void connect(View view) {
        FFDecoder.init(864, 480, surface);
        startServer();
    }

    InputStream is;
    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v(TAG, "开始连接");

                    Socket socket = new Socket("192.168.81.225", 10001);
                    is = socket.getInputStream();
                    Log.v(TAG, "连接成功");

                    while (true) {
                        byte[] head = new byte[4];
                        is.read(head);
                        int len = bufferToInt(head);
                        Log.v(TAG, "读取到长度 " + len);
                        byte[] buf = new byte[len];
                        DataInputStream dis = new DataInputStream(is);
                        dis.readFully(buf);

                        Log.v(TAG, "read content " + Arrays.toString(buf));

                        onFrame(buf, 0, buf.length);
                    }
                } catch (Exception ex) {

                }
            }
        }).start();
    }

    public static int bufferToInt(byte[] src) {
        int value;
        value = (int) ((src[0] & 0xFF)
                | ((src[1] & 0xFF)<<8)
                | ((src[2] & 0xFF)<<16)
                | ((src[3] & 0xFF)<<24));
        return value;
    }

    public void onFrame(byte[] buf, int offset, int length) {
        FFDecoder.decoding(buf);
    }
}
