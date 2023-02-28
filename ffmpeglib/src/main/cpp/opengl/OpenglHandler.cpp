//
// Created by liaojp on 2023/2/25.
//

#include "OpenglHandler.h"

OpenglHandler *OpenglHandler::m_pContext = nullptr;

OpenglHandler::OpenglHandler() {

}

OpenglHandler::~OpenglHandler() {

}

void OpenglHandler::Init(JNIEnv *env, jobject surface) {
    Drawer *drawer = new TriangleDrawer();
    eglRender = new EglRender(env, surface);
    eglRender->SetDrawer(drawer);
}

void OpenglHandler::SetParamsInt(int paramType) {
    Drawer *drawer;
    switch (paramType) {
        case SAMPLE_TYPE_KEY_TRIANGLE:
            drawer = new TriangleDrawer();
            break;
        case SAMPLE_TYPE_KEY_IMAGE:
            drawer = new TextureDrawer();
            break;
        case SAMPLE_TYPE_KEY_YUV_TEXTURE_MAP:
            drawer = new NV21TextureDrawer();
            break;
        default:
            break;
    }
    eglRender->SetDrawer(drawer);
    eglRender->RequestRender();

}

void OpenglHandler::SetScreenSize(int width, int height) {
    eglRender->SetScreenSize(width, height);
}

void OpenglHandler::Render(int format, int width, int height, uint8_t *data, int length) {
    ImageData image;
    image.format = format;
    image.width = width;
    image.height = height;
    image.tempData = data;
    eglRender->Render(&image);
    eglRender->RequestRender();

}

OpenglHandler *OpenglHandler::GetInstance() {
    if (m_pContext == nullptr) {
        m_pContext = new OpenglHandler();
    }
    return m_pContext;
}

void OpenglHandler::DestroyInstance() {
    if (m_pContext) {
        m_pContext->eglRender->Destroy();
        delete m_pContext;
        m_pContext = nullptr;
    }

}





