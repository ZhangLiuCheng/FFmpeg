//
// Created by zhangliucheng on 2017/9/22.
//

#include "com_ylcq_ffmpeg_FFEncoder.h"
#include "log_util.h"

extern "C" {
#include "ffmpeg/include/libavcodec/avcodec.h"
#include "ffmpeg/include/libavutil/opt.h"
#include "ffmpeg/include/libavutil/imgutils.h"

#include "ffmpeg/include/libavformat/avformat.h"
#include "ffmpeg/include/libavutil/avutil.h"
#include "ffmpeg/include/libavutil/frame.h"
#include "ffmpeg/include/libswscale/swscale.h"
}

AVCodec *pCodec;
AVCodecContext *pCodecCtx = NULL;
AVFrame *pFrame;
AVPacket pkt;

int i, ret, got_output;
int y_size;
int framecnt = 0;
int count = 0;
bool flag = false;

JNIEXPORT jint JNICALL Java_com_ylcq_ffmpeg_FFEncoder_init
        (JNIEnv *env, jclass jclazz, jint width, jint height) {
    avcodec_register_all();
    pCodec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (!pCodec) {
        printf("Codec not found\n");
        return -1;
    }
    pCodecCtx = avcodec_alloc_context3(pCodec);
    if (!pCodecCtx) {
        printf("Could not allocate video codec context\n");
        return -1;
    }
    pCodecCtx->bit_rate = 400000;
    pCodecCtx->width = width;
    pCodecCtx->height = height;
    pCodecCtx->time_base.num = 1;
    pCodecCtx->time_base.den = 20;
    pCodecCtx->gop_size = 10;
    pCodecCtx->max_b_frames = 5;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;

//    pCodecCtx->flags |= CODEC_FLAG_GLOBAL_HEADER;

    av_opt_set(pCodecCtx->priv_data, "preset", "superfast", 0);
    av_opt_set(pCodecCtx->priv_data, "tune", "zerolatency", 0);

    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        printf("Could not open codec\n");
        return -1;
    }


    // TODO 打印sps pps
    if (flag == false) {
        uint8_t *data = pCodecCtx->extradata;
        for (int i = 0; i < pCodecCtx->extradata_size; ++i) {
            LOGV("----> %d", data[i]);
        }
        flag = true;
    }
    y_size = pCodecCtx->width * pCodecCtx->height;

    return 1;
}

JNIEXPORT jbyteArray JNICALL Java_com_ylcq_ffmpeg_FFEncoder_encoding
        (JNIEnv *env, jclass jclazz, jbyteArray yuvdata) {

    jsize len = env->GetArrayLength(yuvdata);
    jbyte yuv420sp[len * sizeof(jbyte)];
    env->GetByteArrayRegion(yuvdata, 0, len, yuv420sp);

    pFrame = av_frame_alloc();
    if (!pFrame) {
        printf("Could not allocate video frame\n");
        return NULL;
    }
    pFrame->format = pCodecCtx->pix_fmt;
    pFrame->width = pCodecCtx->width;
    pFrame->height = pCodecCtx->height;

    ret = av_image_alloc(pFrame->data, pFrame->linesize, pCodecCtx->width,
                         pCodecCtx->height, pCodecCtx->pix_fmt, 16);
    if (ret < 0) {
        printf("Could not allocate raw picture buffer\n");
        return NULL;
    }

    av_init_packet(&pkt);
    pkt.data = NULL; // packet data will be allocated by the encoder
    pkt.size = 0;

    //Read raw YUV data  这里出错了，是按YUV_SP处理的 应该是YUV_P
    pFrame->data[0] = yuv420sp; //PCM Data
    pFrame->data[1] = yuv420sp + y_size * 5 / 4; // V
    pFrame->data[2] = yuv420sp + y_size; // U
    pFrame->pts = count;
    count++;
    /* encode the image */
    ret = avcodec_encode_video2(pCodecCtx, &pkt, pFrame, &got_output);
    int sizee = pkt.size;
    if (ret < 0) {
        printf("Error encoding frame\n");
        return NULL;
    }

    if (got_output) {
        printf("Succeed to encode frame: %5d\tsize:%5d\n", framecnt, pkt.size);
        framecnt++;
        jbyteArray encodedData = env->NewByteArray(pkt.size);
        env->SetByteArrayRegion(encodedData, 0, pkt.size, (jbyte *)pkt.data);
        av_frame_free(&pFrame);
        return encodedData;
    }
//    av_free_packet(&pkt);
//    av_freep(&pFrame->data[0]);
//    av_frame_free(&pFrame);
//    env->ReleaseByteArrayElements(yuvdata, yuv420sp, 0);
    return NULL;
}


JNIEXPORT void JNICALL Java_com_ylcq_ffmpeg_FFEncoder_close
        (JNIEnv *env, jclass jclazz) {
    avcodec_close(pCodecCtx);
    av_free(pCodecCtx);
    av_free_packet(&pkt);

//    av_freep(&pFrame->data[0]);
//    av_frame_free(&pFrame);
}