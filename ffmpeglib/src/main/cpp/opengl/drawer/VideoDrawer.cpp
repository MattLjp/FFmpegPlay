//
// Created by Liaojp on 2023/2/24.
//

#include "VideoDrawer.h"

VideoDrawer::VideoDrawer() : Drawer() {
}

VideoDrawer::~VideoDrawer() {

}

const GLchar *VideoDrawer::GetVertexShader() {
    const GLchar *shader = "        #version 300 es \n"
                           "        layout (location = 0) in vec4 aPosition; \n"
                           "        layout (location = 1) in vec2 aCoordinate; \n"
                           "        uniform mat4 uMatrix; \n"
                           "        out vec2 vCoordinate; \n"
                           "        void main() { \n"
                           "            gl_Position  = uMatrix * aPosition; \n"
                           "            vCoordinate = aCoordinate; \n"
                           "        }";

    return shader;
}

const GLchar *VideoDrawer::GetFragmentShader() {
    const GLchar *shader = "        #version 300 es \n"
                           "        precision mediump float; \n"
                           "        in vec2 vCoordinate; \n"
                           "        out vec4 outColor; \n"
                           "        uniform sampler2D uTexture; \n"
                           "        void main() { \n"
                           "            outColor = texture(uTexture, vCoordinate);\n"
                           "        }";

    return shader;
}

void VideoDrawer::InitCstShaderHandler() {

}

void VideoDrawer::BindTexture() {
    ActivateTexture();
}

void VideoDrawer::PrepareDraw() {
    if (cst_data != NULL) {
        glTexImage2D(GL_TEXTURE_2D, 0, // level一般为0
                     GL_RGBA, //纹理内部格式
                     m_video_width, m_video_height, // 画面宽高
                     0, // 必须为0
                     GL_RGBA, // 数据格式，必须和上面的纹理格式保持一直
                     GL_UNSIGNED_BYTE, // RGBA每位数据的字节数，这里是BYTE: 1 byte
                     cst_data);// 画面数据
    }
}

void VideoDrawer::Destroy() {
}
