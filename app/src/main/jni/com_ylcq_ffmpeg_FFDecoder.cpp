//
// Created by zhangliucheng on 2017/9/29.
//

#include "com_ylcq_ffmpeg_FFDecoder.h"
#include "log_util.h"

#include <android/native_window_jni.h>
#include <android/native_window.h>

extern "C" {
#include "ffmpeg/include/libavcodec/avcodec.h"
#include "ffmpeg/include/libavformat/avformat.h"
#include "ffmpeg/include/libswscale/swscale.h"
#include "ffmpeg/include/libavutil/avutil.h"
#include "ffmpeg/include/libavutil/frame.h"
#include "ffmpeg/include/libavutil/imgutils.h"


#include "libyuv/include/libyuv.h"
}

ANativeWindow* nativeWindow;
ANativeWindow_Buffer windowBuffer;
struct SwsContext *sws_ctx;

AVCodec *pCodec;
AVCodecContext *pCodecCtx;
AVPacket packet;
//AVFrame *frame;

int len ,got_frame, framecount = 0;

JNIEXPORT jint JNICALL Java_com_ylcq_ffmpeg_FFDecoder_init
        (JNIEnv * env, jclass jclazz, jint width, jint height, jobject surface) {

    av_register_all();
//    avformat_network_init();
    pCodec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if(pCodec == NULL){
        LOGV("%s","无法解码");
        return -1;
    }
    pCodecCtx = avcodec_alloc_context3(pCodec);
    pCodecCtx->frame_number = 1;
    pCodecCtx->width = width;
    pCodecCtx->height = height;
    pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;

    int ret = avcodec_open2(pCodecCtx, pCodec, NULL);
    if (ret < 0) {
        LOGV("%s","打开解码器失败  " + ret);
        return -1;
    }
//    av_init_packet(&packet);
//    frame = av_frame_alloc();

    ANativeWindow* nw = ANativeWindow_fromSurface(env, surface);
    ANativeWindow_setBuffersGeometry(nw, pCodecCtx->width, pCodecCtx->height, WINDOW_FORMAT_RGBA_8888);
    nativeWindow = nw;

    // 由于解码出来的帧格式不是RGBA的,在渲染之前需要进行格式转换
    sws_ctx = sws_getContext(pCodecCtx->width,
                                                pCodecCtx->height,
                             AV_PIX_FMT_YUV420P,
                                                pCodecCtx->width,
                                                pCodecCtx->height,
                                                AV_PIX_FMT_RGBA,
                                                SWS_BILINEAR,
                                                NULL,
                                                NULL,
                                                NULL);

}


JNIEXPORT jbyteArray JNICALL Java_com_ylcq_ffmpeg_FFDecoder_decoding
        (JNIEnv *env, jclass jclazz, jbyteArray data) {

    // jbyteArray->char*
    jsize len = env->GetArrayLength(data);
//    jbyte jbarray[len * sizeof(jbyte)];
//    env->GetByteArrayRegion(data, 0, len, jbarray);

    char* jbarray = (char*)env->GetByteArrayElements(data, 0);

//    av_init_packet(&packet);
    av_new_packet(&packet, len);
    memcpy(packet.data, jbarray, len);

    LOGV("222222    --->  %d", len);

//    char *sorceData = (char*)jbarray;
//    packet->data = sorceData;
//    packet.data = jbarray;
    LOGV("3333333");

//    packet.size = len;

    LOGV("44444");


    /*
    AVFrame *yuv_frame = av_frame_alloc();
    AVFrame *rgb_frame = av_frame_alloc();

    ANativeWindow_Buffer outBuffer;

    //解码AVPacket->AVFrame
    avcodec_decode_video2(pCodecCtx, yuv_frame, &got_frame, &packet);

    LOGV("5555555 ---->  %d", got_frame);

    if (got_frame) {
        LOGV("解码%d帧", framecount++);


        ANativeWindow_lock(nativeWindow, &outBuffer, NULL);
        LOGV("777777");

        avpicture_fill((AVPicture *) rgb_frame, outBuffer.bits, PIX_FMT_RGBA, pCodecCtx->width, pCodecCtx->height);
        LOGV("888888");

//        //YUV->RGBA_8888
        libyuv::I420ToARGB(yuv_frame->data[0], yuv_frame->linesize[0],
                           yuv_frame->data[2], yuv_frame->linesize[2],
                           yuv_frame->data[1], yuv_frame->linesize[1],
                           rgb_frame->data[0], rgb_frame->linesize[0],
                           pCodecCtx->width, pCodecCtx->height);

        ANativeWindow_unlockAndPost(nativeWindow);
        ANativeWindow_release(nativeWindow);
    }

    av_frame_free(&yuv_frame);
    av_frame_free(&rgb_frame);

    */

    int videoWidth = pCodecCtx->width;
    int videoHeight = pCodecCtx->height;

    // Allocate video frame
    AVFrame * pFrame = av_frame_alloc();

    // 用于渲染
    AVFrame * pFrameRGBA = av_frame_alloc();
    if(pFrameRGBA == NULL || pFrame == NULL) {
        LOGV("Could not allocate video frame.");
        return NULL;
    }

    // Determine required buffer size and allocate buffer
    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, pCodecCtx->width, pCodecCtx->height, 1);
    uint8_t * buffer = (uint8_t *)av_malloc(numBytes * sizeof(uint8_t));
    av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize, buffer, AV_PIX_FMT_RGBA,
                         pCodecCtx->width, pCodecCtx->height, 1);


    //解码AVPacket->AVFrame
    avcodec_decode_video2(pCodecCtx, pFrame, &got_frame, &packet);

    if (got_frame) {
        LOGV("解码%d帧", framecount++);


        // lock native window buffer
        ANativeWindow_lock(nativeWindow, &windowBuffer, 0);

        // 格式转换
        sws_scale(sws_ctx, (uint8_t const *const *) pFrame->data,
                  pFrame->linesize, 0, pCodecCtx->height,
                  pFrameRGBA->data, pFrameRGBA->linesize);

        // 获取stride
        uint8_t *dst = windowBuffer.bits;
        int dstStride = windowBuffer.stride * 4;
        uint8_t *src = (uint8_t *) (pFrameRGBA->data[0]);
        int srcStride = pFrameRGBA->linesize[0];

        // 由于window的stride和帧的stride不同,因此需要逐行复制
        int h;
        for (h = 0; h < videoHeight; h++) {
            memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
        }

        ANativeWindow_unlockAndPost(nativeWindow);

    }

    av_free(buffer);
    av_free(pFrameRGBA);
    av_free(pFrame);
}


JNIEXPORT void JNICALL Java_com_ylcq_ffmpeg_FFDecoder_close
        (JNIEnv *env, jclass jclazz) {
    avcodec_close(pCodecCtx);
//    av_free_packet(packet);
}