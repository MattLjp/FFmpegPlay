//
// Created by liaojp on 2023/2/21.
//

#ifndef FFMPEGPLAY_TRIANGLEDRAWER_H
#define FFMPEGPLAY_TRIANGLEDRAWER_H

#include "Drawer.h"

class TriangleDrawer : public Drawer {
public:
    TriangleDrawer() {}

    ~TriangleDrawer() {}

    const GLchar *GetVertexShader() override;

    const GLchar *GetFragmentShader() override;

    void InitCstShaderHandler() override;

    void BindTexture() override;

    void PrepareDraw() override;

    void DoDraw() override;

    void Destroy() override;

private:

    const GLfloat vertexs[8] = {
            0.0f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f
    };

    const GLfloat colors[12] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    };
};


#endif //OPENGL_TRIANGLEDRAWER_H
