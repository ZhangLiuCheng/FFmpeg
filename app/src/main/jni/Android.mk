LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libavutil
LOCAL_SRC_FILES := ffmpeg/lib/libavutil-54.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libswresample
LOCAL_SRC_FILES := ffmpeg/lib/libswresample-1.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavcodec
LOCAL_SRC_FILES := ffmpeg/lib/libavcodec-56.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libavformat
LOCAL_SRC_FILES := ffmpeg/lib/libavformat-56.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libswscale
LOCAL_SRC_FILES := ffmpeg/lib/libswscale-3.so
include $(PREBUILT_SHARED_LIBRARY)

# libyuv
include $(CLEAR_VARS)
LOCAL_MODULE := libyuv
LOCAL_SRC_FILES := libyuv/lib/libyuv.so
include $(PREBUILT_SHARED_LIBRARY)



include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := libavutil libswresample libavcodec libavformat libswscale
LOCAL_C_INCLUDES := ffmpeg/include
LOCAL_MODULE := libVideoutils
LOCAL_SRC_FILES := com_ylcq_ffmpeg_VidoeUtils.cpp
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS
include $(BUILD_SHARED_LIBRARY)



#ffpusher
include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := libavutil libswresample libavcodec libavformat
LOCAL_C_INCLUDES := ffmpeg/include
LOCAL_MODULE := libFFPusher
LOCAL_SRC_FILES := com_ylcq_ffmpeg_FFPusher.cpp
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -fpermissive
include $(BUILD_SHARED_LIBRARY)



#ffplayer
include $(CLEAR_VARS)
LOCAL_SHARED_LIBRARIES := libavutil libswresample libavcodec libavformat libswscale libyuv
LOCAL_C_INCLUDES += ffmpeg/include
LOCAL_C_INCLUDES += libyuv/include
LOCAL_MODULE := libffplayer
LOCAL_SRC_FILES := com_ylcq_ffmpeg_FFPlayer.cpp
LOCAL_LDLIBS := -llog -landroid
LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -fpermissive
include $(BUILD_SHARED_LIBRARY)