//
// OpenGL EGL核心配置
// Created by cxp on 2019-08-05.
//

#include "egl_core.h"

EglCore::EglCore() {
}

EglCore::~EglCore() {

}

bool EglCore::Init(EGLContext share_ctx, int flags) {
    if (m_egl_dsp != EGL_NO_DISPLAY) {
        LOGE(TAG, "EGL already set up")
        return true;
    }

    if (share_ctx == NULL) {
        share_ctx = EGL_NO_CONTEXT;
    }

    m_egl_dsp = eglGetDisplay(EGL_DEFAULT_DISPLAY);

    if (m_egl_dsp == EGL_NO_DISPLAY || eglGetError() != EGL_SUCCESS) {
        LOGE(TAG, "EGL init display fail")
        return false;
    }

    EGLint major_ver, minor_ver;
    EGLBoolean success = eglInitialize(m_egl_dsp, &major_ver, &minor_ver);
    if (success != EGL_TRUE || eglGetError() != EGL_SUCCESS) {
        LOGE(TAG, "EGL init fail")
        return false;
    }
    // 尝试使用GLES3
    if ((flags & FLAG_TRY_GLES3) != 0) {
        EGLConfig config = GetEGLConfig(flags, 3);
        if (config != NULL) {
            int attrib3_list[] = {
                    EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL_NONE
            };
            EGLContext context = eglCreateContext(m_egl_dsp, config,
                                                  share_ctx, attrib3_list);
            if (eglGetError() == EGL_SUCCESS) {
                m_egl_cfg = config;
                m_egl_cxt = context;
                mGlVersion = 3;
            } else {
                LOGE(TAG, "EGL create fail, error is %x", eglGetError());
            }
        }
    }
    // 如果GLES3没有获取到，则尝试使用GLES2
    if (m_egl_cxt == EGL_NO_CONTEXT) {
        EGLConfig config = GetEGLConfig(flags, 2);
        int attrib2_list[] = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        EGLContext context = eglCreateContext(m_egl_dsp, config,
                                              share_ctx, attrib2_list);
        if (eglGetError() == EGL_SUCCESS) {
            m_egl_cfg = config;
            m_egl_cxt = context;
            mGlVersion = 2;
        } else {
            LOGE(TAG, "EGL create fail, error is %x", eglGetError());
        }
    }
    int values[1] = {0};
    eglQueryContext(m_egl_dsp, m_egl_cxt, EGL_CONTEXT_CLIENT_VERSION, values);
    LOGE("EGLContext created, client version %d", values[0]);

    return true;
}

EGLConfig EglCore::GetEGLConfig(int flags, int version) {
    int renderableType = EGL_OPENGL_ES2_BIT;
    if (version >= 3) {
        renderableType |= EGL_OPENGL_ES3_BIT_KHR;
    }
    int attribList[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_DEPTH_SIZE, 16,
            EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, renderableType,
            EGL_NONE, 0,      // placeholder for recordable [@-3]
            EGL_NONE
    };
    int length = sizeof(attribList) / sizeof(attribList[0]);
    if ((flags & FLAG_RECORDABLE) != 0) {
        attribList[length - 3] = EGL_RECORDABLE_ANDROID;
        attribList[length - 2] = 1;
    }
    EGLConfig configs = NULL;
    int numConfigs;
    if (!eglChooseConfig(m_egl_dsp, attribList, &configs, 1, &numConfigs)) {
        LOGE("unable to find RGB8888 / %d  EGLConfig", version);
        return NULL;
    }
    return configs;
}

EGLSurface EglCore::CreateWindSurface(ANativeWindow *window) {
    EGLSurface surface = eglCreateWindowSurface(m_egl_dsp, m_egl_cfg, window, 0);
    if (eglGetError() != EGL_SUCCESS) {
        LOGI(TAG, "EGL create window surface fail")
        return NULL;
    }
    return surface;
}

EGLSurface EglCore::CreateOffScreenSurface(int width, int height) {
    int CONFIG_ATTRIBS[] = {
            EGL_WIDTH, width,
            EGL_HEIGHT, height,
            EGL_NONE
    };

    EGLSurface surface = eglCreatePbufferSurface(m_egl_dsp, m_egl_cfg, CONFIG_ATTRIBS);
    if (eglGetError() != EGL_SUCCESS) {
        LOGI(TAG, "EGL create off screen surface fail")
        return NULL;
    }
    return surface;
}

void EglCore::MakeCurrent(EGLSurface egl_surface) {
    if (!eglMakeCurrent(m_egl_dsp, egl_surface, egl_surface, m_egl_cxt)) {
        LOGE(TAG, "EGL make current fail");
    }
}

void EglCore::SwapBuffers(EGLSurface egl_surface) {
    eglSwapBuffers(m_egl_dsp, egl_surface);
}

void EglCore::DestroySurface(EGLSurface elg_surface) {
    eglMakeCurrent(m_egl_dsp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroySurface(m_egl_dsp, elg_surface);
}

void EglCore::Release() {
    if (m_egl_dsp != EGL_NO_DISPLAY) {
        eglMakeCurrent(m_egl_dsp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(m_egl_dsp, m_egl_cxt);
        eglReleaseThread();
        eglTerminate(m_egl_dsp);
    }
    m_egl_dsp = EGL_NO_DISPLAY;
    m_egl_cxt = EGL_NO_CONTEXT;
    m_egl_cfg = NULL;
}

int EglCore::getGlVersion() {
    return mGlVersion;
}
