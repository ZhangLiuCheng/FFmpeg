/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_ylcq_ffmpeg_EncodeH264 */

#ifndef _Included_com_ylcq_ffmpeg_EncodeH264
#define _Included_com_ylcq_ffmpeg_EncodeH264
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_ylcq_ffmpeg_FFPusher
 * Method:    getVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_ylcq_ffmpeg_FFPusher_getVersion
        (JNIEnv *, jclass);

/*
 * Class:     com_ylcq_ffmpeg_FFPusher
 * Method:    encoding
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_com_ylcq_ffmpeg_FFPusher_encoding
        (JNIEnv *, jclass, jbyteArray);

/*
 * Class:     com_ylcq_ffmpeg_FFPusher
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_com_ylcq_ffmpeg_FFPusher_close
        (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
