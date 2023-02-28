//
// Created by liaojp on 2023/2/22.
//

#ifndef FFMPEGPLAY_TEXTUREDRAWER_H
#define FFMPEGPLAY_TEXTUREDRAWER_H

#include "Drawer.h"

class TextureDrawer : public Drawer {
public:
    TextureDrawer() {}

    ~TextureDrawer() {}

    const GLchar *GetVertexShader() override;

    const GLchar *GetFragmentShader() override;

    void InitCstShaderHandler() override;

    void BindTexture() override;

    void PrepareDraw() override;

    void Destroy() override;

};


#endif //OPENGL_TEXTUREDRAWER_H
