#ifndef FFMPEGPLAY_LOGUTIL_H
#define FFMPEGPLAY_LOGUTIL_H

#include <android/log.h>
#include <sys/time.h>

#define  LOG_TAG "FFmpegLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__);
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__);
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__);
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__);
#endif
