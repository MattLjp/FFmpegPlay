//
// Created by Liaojp on 2023/1/8.
//

#ifndef FFMPEGPLAY_PLAYER_H
#define FFMPEGPLAY_PLAYER_H

#include <jni.h>
#include "../decoder/video/video_decoder.h"
#include "../decoder/audio/audio_decoder.h"
#include "../render/video/native_render.h"
#include "../render/video/opengl_render.h"
#include "../render/audio/opensl_render.h"
#include "../../opengl/drawer/proxy/DrawerProxy.h"
#include "../../opengl/drawer/proxy/DrawerProxyImpl.h"
#include "../../opengl/drawer/Drawer.h"
#include "../../opengl/drawer/VideoDrawer.h"

#define JAVA_PLAYER_EVENT_CALLBACK_API_NAME "playerEventCallback"

class Player {
private:
    const char *TAG = "Player";
    VideoDecoder *m_video_decoder;
    VideoRender *m_video_render;

    DrawerProxy *m_proxyImpl;
    Drawer *m_drawer;

    AudioDecoder *m_audio_decoder;
    AudioRender *m_audio_render;

    JavaVM *m_JavaVM = nullptr;
    jobject m_JavaObj = nullptr;

    JNIEnv *GetJNIEnv(bool *isAttach);

    jobject GetJavaObj() {
        return m_JavaObj;
    }

    JavaVM *GetJavaVM() {
        return m_JavaVM;
    }

    static void PostMessage(void *context, int msgType, int msgCode);

public:
    ~Player();

    void Init(JNIEnv *jniEnv, jobject obj, char *url, jobject surface);

    void UnInit();

    void Play();

    void Pause();

    void SeekToPosition(int position);

};

#endif //FFMPEGPLAY_PLAYER_H
