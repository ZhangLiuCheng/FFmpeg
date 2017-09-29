package com.ylcq.ffmpeg;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class PusherActivity extends AppCompatActivity implements Camera.PreviewCallback,
        SurfaceHolder.Callback {

    List<Camera.Size> list;
    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;
    Camera mCamera;
    TextView tv;
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    byte[] bytedata = msg.getData().getByteArray("messageyuvdata");
                    if (bytedata != null) {
                        // tv.setText(temp+"");
                        int count = addVideoData(bytedata);
                        tv.setText("length:"+length);
                    }
                    break;
                case 2 :
                    String s = msg.getData().getString("supportFrameSize");
                    tv.setText(s);
                    break;
            }
        };
    };

    int temp = 0;
    int length = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pusher);

//        File rootFile = getExternalCacheDir();
//        String dest = rootFile.getAbsolutePath() + "/camera.h264";
//        Log.v("Qingyuan", dest);

        tv = (TextView) findViewById(R.id.tv);
        temp = FFPusher.getVersion();
        Log.v("QingYuan", "FFPusher.getVersion()=" + temp);
        if (temp != 1) {
            Toast.makeText(PusherActivity.this, "FFPusher打开失败", Toast.LENGTH_SHORT).show();
            return;
        }
        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        length = data.length;
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

        mCamera.setPreviewCallback(PusherActivity.this);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
    }

    public synchronized int addVideoData(byte[] data) {
        int s = FFPusher.encoding(data);
        Log.v("QingYuan", "addVideoData  " + s);
        return s;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FFPusher.close();
    }
}
