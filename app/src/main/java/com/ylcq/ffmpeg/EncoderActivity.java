package com.ylcq.ffmpeg;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class EncoderActivity extends AppCompatActivity implements Camera.PreviewCallback,
        SurfaceHolder.Callback {

    List<Camera.Size> list;
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    Camera mCamera;
    TextView tv;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    byte[] bytedata = msg.getData().getByteArray("messageyuvdata");
                    if (bytedata != null) {
                        addVideoData(bytedata);
                    }
                    break;
                case 2 :
                    String s = msg.getData().getString("supportFrameSize");
                    tv.setText(s);
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoder);

        tv = (TextView) findViewById(R.id.tv);
        int ret = FFEncoder.init(864, 480);
        Log.v("QingYuan", "FFPusher.getVersion()=" + ret);
        if (ret != 1) {
            Toast.makeText(EncoderActivity.this, "FFPusher打开失败", Toast.LENGTH_SHORT).show();
            return;
        }
        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);

        startServer();
    }

    // TODO ------------------------------- begin ----------------------------
    private OutputStream os;
    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSockets = new ServerSocket(10001);
                    Socket socket = serverSockets.accept();
                    os = socket.getOutputStream();
                } catch (Exception ex) {

                }
            }
        }).start();
    }
    public static byte[] intToBuffer(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value>>24) & 0xFF);
        src[2] = (byte) ((value>>16) & 0xFF);
        src[1] = (byte) ((value>>8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }
    // TODO ------------------------------- end ----------------------------

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Message msg = new Message();
        Bundle bl = new Bundle();
        bl.putByteArray("messageyuvdata", data);
        msg.setData(bl);
        msg.what = 1;
        mHandler.sendMessage(msg);

//        int count = addVideoData(data);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 打开前置摄像头
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Integer> supportedPictureFormats = parameters
                    .getSupportedPictureFormats();
            for (Integer integer : supportedPictureFormats) {
                Log.i("sun", integer + "");
            }
            list = parameters.getSupportedPreviewSizes();
            parameters.setPreviewSize(864, 480);
            parameters.setPreviewFormat(ImageFormat.YV12);
//            parameters.setPreviewFpsRange(20, 30); // 每秒显示20~30帧
            parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
//            parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            String supportFrameSize = null;
            for (int i = 0; i < list.size(); i++) {
                int width =list.get(i).width;
                int height =list.get(i).height;
                supportFrameSize = supportFrameSize+width+"-"+height+"||||||";
            }


            Message msg = new Message();
            Bundle bl = new Bundle();
            bl.putString("supportFrameSize", supportFrameSize);
            msg.setData(bl);
            msg.what = 2;
            mHandler.sendMessage(msg);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 开始预览
        try {
            // 设置哪个surfaceView显示图片
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 设置预览帧的接口,就是通过这个接口，我们来获得预览帧的数据的

        mCamera.setPreviewCallback(EncoderActivity.this);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
    }

    public synchronized void addVideoData(byte[] data) {
//        byte[] encodedData = FFEncoder.encoding(data);
//        if (null != encodedData) {
//            Log.v("QingYuan", "addVideoData  " + Arrays.toString(encodedData));
//        }

        // TODO ---------------------------

        if (null != os) {
            byte[] encodedData = FFEncoder.encoding(data);
            if (null == encodedData || encodedData.length == 0) {
                Log.v("QingYuan", "addVideoData  " + encodedData);
                return;
            } else {
                Log.v("QingYuan", "addVideoData  " + Arrays.toString(encodedData));
            }
            try {
                byte[] buf = encodedData;
                byte[] bytes = new byte[buf.length + 4];
                byte[] head = intToBuffer(buf.length);
                System.arraycopy(head, 0, bytes, 0, head.length);
                System.arraycopy(buf, 0, bytes, head.length, buf.length);
                os.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FFEncoder.close();
    }
}
