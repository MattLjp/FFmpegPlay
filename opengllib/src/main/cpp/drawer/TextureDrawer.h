//
// Created by liaojp on 2023/2/22.
//

#ifndef FFMPEGPLAY_TEXTUREDRAWER_H
#define FFMPEGPLAY_TEXTUREDRAWER_H

#include "IDrawer.h"

class TextureDrawer : public IDrawer {
public:
    TextureDrawer() {}

    ~TextureDrawer() {}

    void Create() override;

    void LoadImage(ImageData *pImage) override;

    void SetWorldSize(int worldW, int worldH) override;

    void Draw() override;

    void Release() override;

private:
    GLuint mTextureId;
    GLint vTexCoordHandle;
    ImageData mRenderImage;
};


#endif //FFMPEGPLAY_TEXTUREDRAWER_H
