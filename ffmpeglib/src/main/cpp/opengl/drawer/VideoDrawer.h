//
// Created by Liaojp on 2023/2/24.
//

#ifndef FFMPEGPLAY_VIDEO_DRAWER_H
#define FFMPEGPLAY_VIDEO_DRAWER_H


#include "Drawer.h"

class VideoDrawer : public Drawer {
public:

    VideoDrawer();

    ~VideoDrawer();

    const GLchar *GetVertexShader() override;

    const GLchar *GetFragmentShader() override;

    void InitCstShaderHandler() override;

    void BindTexture() override;

    void PrepareDraw() override;

    void Destroy() override;
};


#endif //OPENGL_VIDEO_DRAWER_H
