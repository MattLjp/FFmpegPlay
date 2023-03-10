//
// OpenGL EGL核心配置
// Created by cxp on 2019-08-05.
//

#ifndef LEARNVIDEO_EGL_CORE_H
#define LEARNVIDEO_EGL_CORE_H

#include "../../utils/LogUtil.h"

extern "C" {
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <EGL/eglplatform.h>
};

/**
 * Constructor flag: surface must be recordable.  This discourages EGL from using a
 * pixel format that cannot be converted efficiently to something usable by the video
 * encoder.
 */
#define FLAG_RECORDABLE 0x01

/**
 * Constructor flag: ask for GLES3, fall back to GLES2 if not available.  Without this
 * flag, GLES2 is used.
 */
#define FLAG_TRY_GLES3 002

// Android-specific extension
#define EGL_RECORDABLE_ANDROID 0x3142

class EglCore {
private:

    const char *TAG = "EglCore";

    // EGL显示窗口
    EGLDisplay m_egl_dsp = EGL_NO_DISPLAY;

    // EGL上线问
    EGLContext m_egl_cxt = EGL_NO_CONTEXT;

    // EGL配置
    EGLConfig m_egl_cfg;

    EGLConfig GetEGLConfig(int flags, int version);

    int mGlVersion = -1;
public:
    EglCore();

    ~EglCore();

    bool Init(EGLContext share_ctx, int flags);

    /**
     * 根据本地窗口创建显示表面
     * @param window 本地窗口
     * @return
     */
    EGLSurface CreateWindSurface(ANativeWindow *window);

    /**
     * 创建离屏渲染表面
     * @param width 表面宽
     * @param height 表面高
     * @return
     */
    EGLSurface CreateOffScreenSurface(int width, int height);

    /**
     * 将OpenGL上下文和线程进行绑定
     * @param egl_surface
     */
    void MakeCurrent(EGLSurface egl_surface);

    /**
     * 将缓存数据交换到前台进行显示
     * @param egl_surface
     */
    void SwapBuffers(EGLSurface egl_surface);

    /**
     * 释放显示
     * @param elg_surface
     */
    void DestroySurface(EGLSurface elg_surface);

    /**
     * 释放ELG
     */
    void Release();

    // 获取当前的GLES 版本号
    int getGlVersion();
};


#endif //LEARNVIDEO_EGL_CORE_H
