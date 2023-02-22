//
// Created by liaojp on 2023/2/21.
//

#ifndef FFMPEGPLAY_SIMPLERENDER_H
#define FFMPEGPLAY_SIMPLERENDER_H



#include <vector>
#include "../drawer/IDrawer.h"
#include "../drawer/TriangleDrawer.h"
#include "../drawer/TextureDrawer.h"
using namespace std;

#define SAMPLE_TYPE_KEY_TRIANGLE 100
#define SAMPLE_TYPE_KEY_IMAGE 101

class SimpleRender {
public:
    SimpleRender();

    ~SimpleRender();

    void SetDrawerType(int type);

    void SetImageData(int format, int width, int height, uint8_t *pData);

    void OnSurfaceCreated();

    void OnSurfaceChanged(int width, int height);

    void OnDrawFrame();

    void SetDrawer(IDrawer *drawer) {
        drawers.clear();
        drawers.push_back(drawer);
        refreshFlag = true;
    }

    void AddDrawer(IDrawer *drawer) {
        drawers.push_back(drawer);
    }

    static SimpleRender *GetInstance();

    static void DestroyInstance();

private:
    void Destroy() {
        for (int i = 0; i < drawers.size(); i++) {
            IDrawer *drawer = drawers[i];
            drawer->Release();
            delete drawer;
        }
        drawers.clear();
    }

    static SimpleRender *mContext;

    vector<IDrawer *> drawers;

    int width;

    int height;

    bool refreshFlag;

};


#endif //FFMPEGPLAY_SIMPLERENDER_H
