//
// Created by liaojp on 2023/2/27.
//

#include "EglRender.h"

EglRender::EglRender(JNIEnv *env, jobject surface) {
    this->m_env = env;
    //获取JVM虚拟机，为创建线程作准备
    env->GetJavaVM(&m_jvm_for_thread);
    InitRenderThread();

    m_drawer_proxy = new DrawerProxyImpl();
    if (NULL != surface) {
        m_surface_ref = m_env->NewGlobalRef(surface);
        m_state = FRESH_SURFACE;
    } else {
        m_env->DeleteGlobalRef(m_surface_ref);
        m_state = SURFACE_DESTROY;
    }
}

EglRender::~EglRender() {

}

void EglRender::InitRenderThread() {
    // 使用智能指针，线程结束时，自动删除本类指针
    std::shared_ptr<EglRender> that(this);
    std::thread t(sRenderThread, that);
    t.detach();
}

void EglRender::sRenderThread(std::shared_ptr<EglRender> that) {
    JNIEnv *env;

    //将线程附加到虚拟机，并获取env
    if (that->m_jvm_for_thread->AttachCurrentThread(&env, NULL) != JNI_OK) {
        LOGE(that->TAG, "线程初始化异常");
        return;
    }

    if (!that->InitEGL()) {
        //解除线程和jvm关联
        that->m_jvm_for_thread->DetachCurrentThread();
        return;
    }

    while (true) {
        switch (that->m_state) {
            case FRESH_SURFACE:
                LOGI(that->TAG, "Loop Render FRESH_SURFACE")
                that->InitDspWindow(env);
                that->CreateSurface();
                that->m_state = RENDERING;
                break;
            case RENDERING:
                that->Render();
                if (that->mRenderMode == RENDER_WHEN_DIRTY) {
                    that->wait();
                }
                break;
            case SURFACE_DESTROY:
                LOGI(that->TAG, "Loop Render SURFACE_DESTROY")
                that->ReleaseRender();
                that->m_state = NO_SURFACE;
                break;
            case STOP:
                LOGI(that->TAG, "Loop Render STOP")
                //解除线程和jvm关联
                that->ReleaseRender();
                that->m_jvm_for_thread->DetachCurrentThread();
                return;
            case NO_SURFACE:
            default:
                break;
        }
        usleep(20000);
    }
}

bool EglRender::InitEGL() {
    m_egl_surface = new EglSurface();
    return m_egl_surface->Init();
}

void EglRender::InitDspWindow(JNIEnv *env) {
    if (m_surface_ref != NULL) {
        // 初始化窗口
        m_native_window = ANativeWindow_fromSurface(env, m_surface_ref);

        // 绘制区域的宽高
        m_window_width = ANativeWindow_getWidth(m_native_window);
        m_window_height = ANativeWindow_getHeight(m_native_window);

        //设置宽高限制缓冲区中的像素数量
        ANativeWindow_setBuffersGeometry(m_native_window, m_window_width,
                                         m_window_height, WINDOW_FORMAT_RGBA_8888);

        LOGD(TAG, "View Port width: %d, height: %d", m_window_width, m_window_height)
    }
}

void EglRender::CreateSurface() {
    glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    //开启混合，即半透明
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    m_egl_surface->CreateEglSurface(m_native_window, m_window_width, m_window_height);
    glViewport(0, 0, m_window_width, m_window_height);
}

void EglRender::Render() {
    if (RENDERING == m_state) {
        glClear(GL_COLOR_BUFFER_BIT);

        m_drawer_proxy->Draw();
        m_egl_surface->SwapBuffers();

        if (m_need_output_pixels && m_pixel_receiver != NULL) {//输出画面rgba
            m_need_output_pixels = false;
            Render(); //再次渲染最新的画面

            size_t size = m_window_width * m_window_height * 4 * sizeof(uint8_t);

            uint8_t *rgb = (uint8_t *) malloc(size);
            if (rgb == NULL) {
                realloc(rgb, size);
                LOGE(TAG, "内存分配失败： %d", rgb)
            }
            glReadPixels(0, 0, m_window_width, m_window_height, GL_RGBA, GL_UNSIGNED_BYTE, rgb);
            m_pixel_receiver->ReceivePixel(rgb);
        }
    }
}

void EglRender::ReleaseRender() {
    if (m_egl_surface != NULL) {
        m_egl_surface->Release();
        delete m_egl_surface;
        m_egl_surface = NULL;
    }
    if (m_native_window != NULL) {
        ANativeWindow_release(m_native_window);
        m_native_window = NULL;
    }
    if (m_drawer_proxy != NULL) {
        m_drawer_proxy->Release();
        delete m_drawer_proxy;
        m_drawer_proxy = NULL;
    }
}


void EglRender::SetDrawer(Drawer *drawer) {
    m_drawer_proxy->SetDrawer(drawer);
}


void EglRender::Render(ImageData* data) {
    m_drawer_proxy->Render(data);
}

void EglRender::Destroy() {
    m_state = STOP;
    notifyGo();
}

void EglRender::RequestRender() {
    notifyGo();
}


void EglRender::SetScreenSize(int width, int height) {
    m_drawer_proxy->SetScreenSize(width, height);
    m_window_width = width;
    m_window_height = height;
    m_state = FRESH_SURFACE;
}

void EglRender::RequestRgbaData() {
    m_need_output_pixels = true;
}



