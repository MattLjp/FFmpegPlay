//
// Created by liaojp on 2023/2/21.
//

#ifndef FFMPEGPLAY_IDRAWER_H
#define FFMPEGPLAY_IDRAWER_H

#include <GLES3/gl3.h>
#include "../utils/ShaderUtils.h"
#include "../utils/ImageData.h"
#include "../utils/TextureUtils.h"

class IDrawer {
public:
    IDrawer() {}

    virtual ~IDrawer() {}

    virtual void Create() = 0;

    virtual void LoadImage(ImageData *pImage) = 0;

    virtual void SetWorldSize(int worldW, int worldH) = 0;

    virtual void Draw() = 0;

    virtual void Release() = 0;

protected:
    GLuint mProgramId = 0;
};

#endif //FFMPEGPLAY_IDRAWER_H
