//
// Created by liaojp on 2023/2/27.
//

#ifndef FFMPEGPLAY_EGLRENDER_H
#define FFMPEGPLAY_EGLRENDER_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <jni.h>
#include <memory>
#include <thread>
#include <unistd.h>
#include <libavutil/mem.h>
#include <thread>
#include "../media/render/video/opengl_pixel_receiver.h"
#include "../egl/egl_surface.h"
#include "../drawer/proxy/DrawerProxyImpl.h"
#include "../drawer/Drawer.h"
#include "../utils/ImageData.h"

#define RENDER_CONTINUOUSLY 1
#define RENDER_WHEN_DIRTY 2

class EglRender {
private:
    const char *TAG = "EglRender";

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

    int mRenderMode = RENDER_WHEN_DIRTY;

    std::mutex m_mutex;
    std::condition_variable m_cond;

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
    static void sRenderThread(std::shared_ptr<EglRender> that);

    void wait() {
        std::unique_lock<std::mutex> lock(m_mutex);
        m_cond.wait(lock);
    }

    void notifyGo() {
        std::unique_lock<std::mutex> lock(m_mutex);
        m_cond.notify_all();
    }

public:
    EglRender(JNIEnv *env, jobject surface);

    ~EglRender();

    void SetDrawer(Drawer *drawer);

    void SetScreenSize(int width, int height);

    void Render(ImageData* data);

    void RequestRender();

    void Destroy();

    void SetPixelReceiver(OpenGLPixelReceiver *receiver) {
        m_pixel_receiver = receiver;
    }

    void RequestRgbaData();

    void SetRenderMode(int mode) {
        mRenderMode = mode;
    }
};


#endif //FFMPEGPLAY_EGLRENDER_H
