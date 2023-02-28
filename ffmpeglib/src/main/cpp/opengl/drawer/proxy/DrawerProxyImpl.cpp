//
// 默认画面渲染代理
// Created by Liaojp on 2023/2/24.
//

#include "DrawerProxyImpl.h"

void DrawerProxyImpl::SetDrawer(Drawer *drawer) {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->Release();
        delete m_drawers[i];
    }

    m_drawers.clear();
    m_drawers.push_back(drawer);
    drawer->SetWorldSize(screenWidth, screenHeight);
}

void DrawerProxyImpl::AddDrawer(Drawer *drawer) {
    m_drawers.push_back(drawer);
    drawer->SetWorldSize(screenWidth, screenHeight);
}

void DrawerProxyImpl::SetDrawerSize(int width, int height) {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->SetVideoSize(width, height);
    }
}

void DrawerProxyImpl::SetScreenSize(int width, int height) {
    screenWidth = width;
    screenHeight = height;
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->SetWorldSize(width, height);
    }
}

void DrawerProxyImpl::Render(uint8_t *data) {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->Render(data);
    }
}

void DrawerProxyImpl::Render(ImageData* data) {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->Render(data);
    }
}



void DrawerProxyImpl::Draw() {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->Draw();
    }
}

void DrawerProxyImpl::Release() {
    for (int i = 0; i < m_drawers.size(); ++i) {
        m_drawers[i]->Release();
        delete m_drawers[i];
    }
    m_drawers.clear();
}


