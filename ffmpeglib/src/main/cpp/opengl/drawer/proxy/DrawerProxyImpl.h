//
// 默认画面渲染代理
// Created by Liaojp on 2023/2/24.
//

#ifndef FFMPEGPLAY_DRAWER_PROXY_IMPL_H
#define FFMPEGPLAY_DRAWER_PROXY_IMPL_H


#include "DrawerProxy.h"
#include <vector>
#include "../../../utils/LogUtil.h"

class DrawerProxyImpl : public DrawerProxy {

private:
    std::vector<Drawer *> m_drawers;

public:
    void SetDrawer(Drawer *drawer)override;

    void AddDrawer(Drawer *drawer)override;

    void SetDrawerSize(int width, int height) override;

    void SetScreenSize(int width, int height) override;

    void Render(uint8_t *data) override;

    void Render(ImageData* data) override;

    void Draw() override;

    void Release() override;
};


#endif //OPENGL_DRAWER_PROXY_IMPL_H
