//
// Created by liaojp on 21/12/2022.
//

#include <jni.h>
#include <string>
#include "utils/LogUtil.h"
#include "media/player/player.h"


extern "C" {
#include <libavcodec/version.h>
#include <libavcodec/avcodec.h>
#include <libavformat/version.h>
#include <libavutil/version.h>
#include <libavfilter/version.h>
#include <libswresample/version.h>
#include <libswscale/version.h>
};

extern "C"
JNIEXPORT jstring JNICALL
Java_com_matt_ffmpeglib_FFmpegPlay_00024Companion_getFFmpegVersion(JNIEnv *env, jobject thiz) {
    char strBuffer[1024 * 4] = {0};
    strcat(strBuffer, "libavcodec : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVCODEC_VERSION));
    strcat(strBuffer, "\nlibavformat : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVFORMAT_VERSION));
    strcat(strBuffer, "\nlibavutil : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVUTIL_VERSION));
    strcat(strBuffer, "\nlibavfilter : ");
    strcat(strBuffer, AV_STRINGIFY(LIBAVFILTER_VERSION));
    strcat(strBuffer, "\nlibswresample : ");
    strcat(strBuffer, AV_STRINGIFY(LIBSWRESAMPLE_VERSION));
    strcat(strBuffer, "\nlibswscale : ");
    strcat(strBuffer, AV_STRINGIFY(LIBSWSCALE_VERSION));
//    strcat(strBuffer, "\navcodec_configure : \n");
//    strcat(strBuffer, avcodec_configuration());
    strcat(strBuffer, "\navcodec_license : ");
    strcat(strBuffer, avcodec_license());
    LOGD("FFmpegLib::GetFFmpegVersion\n%s", strBuffer);
    return env->NewStringUTF(strBuffer);
}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_matt_ffmpeglib_FFmpegPlay_create(JNIEnv *env, jobject thiz, jstring jurl, jobject surface) {
    const char *url = env->GetStringUTFChars(jurl, NULL);
    if (url == NULL) {
        env->ReleaseStringUTFChars(jurl, url);
        return -1;
    }
    Player *player = new Player();
    player->Init(env, thiz, const_cast<char *>(url), surface, 0);
    env->ReleaseStringUTFChars(jurl, url);
    return reinterpret_cast<jlong>(player);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_matt_ffmpeglib_FFmpegPlay_play(JNIEnv *env, jobject thiz, jlong player_handle) {
    if (player_handle != -1) {
        Player *player = reinterpret_cast<Player *>(player_handle);
        player->Play();
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_matt_ffmpeglib_FFmpegPlay_pause(JNIEnv *env, jobject thiz, jlong player_handle) {
    if (player_handle != -1) {
        Player *player = reinterpret_cast<Player *>(player_handle);
        player->Pause();
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_matt_ffmpeglib_FFmpegPlay_stop(JNIEnv *env, jobject thiz, jlong player_handle) {
    if (player_handle != -1) {
        Player *player = reinterpret_cast<Player *>(player_handle);
        delete player;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_matt_ffmpeglib_FFmpegPlay_seekToPosition(JNIEnv *env, jobject thiz, jlong player_handle, jint progress) {
    if (player_handle != -1) {
        Player *player = reinterpret_cast<Player *>(player_handle);
        player->SeekToPosition(progress);
    }
}