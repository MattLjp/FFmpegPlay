//
// Created by liaojp on 2023/2/21.
//

#include "TriangleDrawer.h"


const GLchar *TriangleDrawer::GetVertexShader() {
    const GLchar *shader = "        #version 300 es \n"
                           "        layout (location = 0) in vec4 aPosition;\n"
                           "        layout (location = 1) in vec4 aColor;\n"
                           "        out vec4 vCoordinate; \n"
                           "        void main() { \n"
                           "            gl_Position  = aPosition; \n"
                           "            vCoordinate = aColor; \n"
                           "        }";

    return shader;
}

const GLchar *TriangleDrawer::GetFragmentShader() {
    const GLchar *shader = "        #version 300 es \n"
                           "        precision mediump float; \n"
                           "        in vec4 vCoordinate; \n"
                           "        out vec4 outColor; \n"
                           "        void main() { \n"
                           "            outColor = vCoordinate; \n"
                           "        }";

    return shader;
}

void TriangleDrawer::InitCstShaderHandler() {

}

void TriangleDrawer::BindTexture() {

}

void TriangleDrawer::PrepareDraw() {

}

void TriangleDrawer::Destroy() {

}


void TriangleDrawer::DoDraw() {
    //启用顶点的句柄
    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);
    //设置着色器参数
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, vertexs);
    glVertexAttribPointer(1, 4, GL_FLOAT, GL_FALSE, 0, colors);
    //开始绘制
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}
