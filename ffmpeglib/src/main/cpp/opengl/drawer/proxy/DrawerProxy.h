//
// Created by Liaojp on 2023/2/24.
//

#ifndef FFMPEGPLAY_DRAWER_PROXY_H
#define FFMPEGPLAY_DRAWER_PROXY_H


#include "../Drawer.h"

class DrawerProxy {
public:
    virtual void SetDrawer(Drawer *drawer) = 0;

    virtual void AddDrawer(Drawer *drawer) = 0;

    virtual void SetDrawerSize(int width, int height) = 0;

    virtual void SetScreenSize(int width, int height) = 0;

    virtual void Render(uint8_t *data) = 0;

    virtual void Render(ImageData* data) = 0;

    virtual void Draw() = 0;

    virtual void Release() = 0;

    virtual ~DrawerProxy() {}

protected:
    int screenWidth;
    int screenHeight;
};


#endif //OPENGL_DRAWER_PROXY_H
