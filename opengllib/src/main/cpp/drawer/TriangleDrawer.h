//
// Created by liaojp on 2023/2/21.
//

#ifndef FFMPEGPLAY_TRIANGLEDRAWER_H
#define FFMPEGPLAY_TRIANGLEDRAWER_H

#include "IDrawer.h"

class TriangleDrawer : public IDrawer {
public:
    TriangleDrawer() {}

    ~TriangleDrawer() {}

    void Create() override;

    void LoadImage(ImageData *pImage) override;

    void SetWorldSize(int worldW, int worldH) override;

    void Draw() override;

    void Release() override;
};


#endif //FFMPEGPLAY_TRIANGLEDRAWER_H
