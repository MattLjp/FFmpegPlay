//
// Created by Liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_OPENGL_RENDER_H
#define FFMPEGPLAY_OPENGL_RENDER_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <jni.h>
#include <memory>
#include <thread>
#include <unistd.h>
#include <libavutil/mem.h>
#include "video_render.h"
#include "opengl_pixel_receiver.h"
#include "../../../opengl/egl/egl_surface.h"
#include "../../../opengl/drawer/proxy/DrawerProxy.h"

class OpenGLRender : public VideoRender {
private:

    const char *TAG = "OpenGLRender";

    enum STATE {
        NO_SURFACE, //没有有效的surface
        FRESH_SURFACE, //持有一个未初始化的新的surface
        RENDERING, //初始化完毕，可以开始渲染
        SURFACE_DESTROY, //surface销毁
        STOP //停止绘制
    };

    JNIEnv *m_env = NULL;

    // 线程依附的JVM环境
    JavaVM *m_jvm_for_thread = NULL;

    // Surface引用，必须使用引用，否则无法在线程中操作
    jobject m_surface_ref = NULL;

    // 本地屏幕
    ANativeWindow *m_native_window = NULL;

    // EGL显示表面
    EglSurface *m_egl_surface = NULL;

    // 绘制代理器
    DrawerProxy *m_drawer_proxy = NULL;

    int m_window_width = 0;
    int m_window_height = 0;

    bool m_need_output_pixels = false;

    OpenGLPixelReceiver *m_pixel_receiver = NULL;

    STATE m_state = NO_SURFACE;

    // 初始化相关的方法
    void InitRenderThread();

    bool InitEGL();

    void InitDspWindow(JNIEnv *env);

    // 创建Surface
    void CreateSurface();

    // 渲染方法
    void Render();

    // 释放资源相关方法
    void ReleaseRender();


    // 渲染线程回调方法
    static void sRenderThread(std::shared_ptr<OpenGLRender> that);

public:
    OpenGLRender(JNIEnv *env, DrawerProxy *drawer_proxy, jobject surface);

    ~OpenGLRender();

    void InitRender(int video_width, int video_height, int *dst_size) override;

    void RenderVideoFrame(OneFrame *one_frame) override;

    void UnInitRender() override;

    void SetScreenSize(int width, int height);

    void SetPixelReceiver(OpenGLPixelReceiver *receiver) {
        m_pixel_receiver = receiver;
    }
    void RequestRgbaData();

};


#endif //FFMPEGPLAY_OPENGL_RENDER_H
