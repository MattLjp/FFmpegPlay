//
// Created by liaojp on 2023/2/21.
//

#include "SimpleRender.h"

SimpleRender::SimpleRender() {

}

SimpleRender::~SimpleRender() {

}

void SimpleRender::SetDrawerType(int type) {
    IDrawer *drawer;
    switch (type) {
        case SAMPLE_TYPE_KEY_TRIANGLE:
            drawer = new TriangleDrawer();
        case SAMPLE_TYPE_KEY_IMAGE:
            drawer = new TextureDrawer();
            break;
    }
    SetDrawer(drawer);
}

void SimpleRender::SetImageData(int format, int width, int height, uint8_t *pData) {
    ImageData imageData;
    imageData.format = format;
    imageData.width = width;
    imageData.height = width;
    imageData.ppPlane[0] = pData;
    for (int i = 0; i < drawers.size(); i++) {
        drawers[i]->LoadImage(&imageData);
    }
}

void SimpleRender::OnSurfaceCreated() {
    //设置背景颜色
    glClearColor(0.0, 0.0, 0.0, 0.0);
    //开启混合，即半透明
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    for (int i = 0; i < drawers.size(); i++) {
        drawers[i]->Create();
    }
}

void SimpleRender::OnSurfaceChanged(int width, int height) {
    this->width = width;
    this->height = height;
    glViewport(0, 0, width, height);
    for (int i = 0; i < drawers.size(); i++) {
        drawers[i]->SetWorldSize(width, height);
    }
}

void SimpleRender::OnDrawFrame() {
    if (refreshFlag) {
        refreshFlag = false;
        for (int i = 0; i < drawers.size(); i++) {
            drawers[i]->Release();
        }
        OnSurfaceCreated();
        OnSurfaceChanged(width, height);
        OnDrawFrame();
    } else {
        glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        for (int i = 0; i < drawers.size(); i++) {
            drawers[i]->Draw();
        }
    }
}

SimpleRender *SimpleRender::mContext = nullptr;

SimpleRender *SimpleRender::GetInstance() {
    if (mContext == nullptr) {
        mContext = new SimpleRender();
    }
    return mContext;
}

void SimpleRender::DestroyInstance() {
    if (mContext) {
        mContext->Destroy();
        delete mContext;
        mContext = nullptr;
    }
}


