//
// Created by liaojp on 2023/2/25.
//

#ifndef FFMPEGPLAY_OPENGLHANDLER_H
#define FFMPEGPLAY_OPENGLHANDLER_H

#include "stdint.h"
#include "../media/render/video/opengl_pixel_receiver.h"
#include "drawer/proxy/DrawerProxyImpl.h"
#include "egl/egl_surface.h"
#include "EglRender.h"
#include "drawer/TextureDrawer.h"
#include "drawer/TriangleDrawer.h"
#include "drawer/VideoDrawer.h"
#include "drawer/NV21TextureDrawer.h"


#define SAMPLE_TYPE_KEY_TRIANGLE 100
#define SAMPLE_TYPE_KEY_IMAGE 101
#define SAMPLE_TYPE_KEY_YUV_TEXTURE_MAP 102

class OpenglHandler {
    OpenglHandler();

    ~OpenglHandler();

private:
    EglRender *eglRender;
    static OpenglHandler *m_pContext;

public:
    void Init(JNIEnv *env, jobject surface);

    void SetParamsInt(int paramType);

    void SetScreenSize(int width, int height);

    void Render(int format, int width, int height, uint8_t *data, int length);

    static OpenglHandler *GetInstance();

    static void DestroyInstance();
};


#endif //FFMPEGPLAY_OPENGLHANDLER_H
