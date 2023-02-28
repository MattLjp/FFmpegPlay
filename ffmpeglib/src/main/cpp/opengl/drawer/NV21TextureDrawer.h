//
// Created by liaojp on 2023/2/27.
//

#ifndef FFMPEGPLAY_NV21TEXTUREDRAWER_H
#define FFMPEGPLAY_NV21TEXTUREDRAWER_H

#include "Drawer.h"
class NV21TextureDrawer : public Drawer {
public:
    NV21TextureDrawer() {}

    ~NV21TextureDrawer() {}

    const GLchar *GetVertexShader() override;

    const GLchar *GetFragmentShader() override;

    void InitCstShaderHandler() override;

    void BindTexture() override;

    void PrepareDraw() override;

    void Destroy() override;

private:
    int y_texture_handler;
    int uv_texture_handler;
    GLuint textureIds[2];
};


#endif //FFMPEGPLAY_NV21TEXTUREDRAWER_H
