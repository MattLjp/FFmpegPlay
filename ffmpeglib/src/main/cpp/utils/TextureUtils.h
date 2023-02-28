//
// Created by liaojp on 2023/2/22.
//

#ifndef FFMPEGPLAY_TEXTUREUTILS_H
#define FFMPEGPLAY_TEXTUREUTILS_H

extern "C" {
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
};
class TextureUtils {
public:
    static void CreateTextureID(GLuint *textures) {
        glGenTextures(1, textures);
        glBindTexture(GL_TEXTURE_2D, *textures);
        SetTexParameter();
        glBindTexture(GL_TEXTURE_2D, GL_NONE);
    }

private:
    static void SetTexParameter() {
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }
};

#endif //FFMPEGPLAY_TEXTUREUTILS_H
