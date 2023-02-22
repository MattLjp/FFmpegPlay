//
// Created by liaojp on 2023/2/21.
//

#include "TriangleDrawer.h"

static char vShaderStr[] =
        "        #version 300 es \n"
        "        layout (location = 0) in vec4 vPosition;\n"
        "        layout (location = 1) in vec4 aColor;\n"
        "        out vec4 vColor;\n"
        "        void main() { \n"
        "            gl_Position  = vPosition;\n"
        "            gl_PointSize = 10.0;\n"
        "            vColor = aColor;\n"
        "        }";
static char fShaderStr[] =
        "        #version 300 es \n"
        "        precision mediump float;\n"
        "        in vec4 vColor;\n"
        "        out vec4 fragColor;\n"
        "        void main() { \n"
        "            fragColor = vColor; \n"
        "        }";

static GLfloat vertices[] = {
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
};

static GLfloat colors[] = {
        0.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f
};

void TriangleDrawer::Create() {
    mProgramId = ShaderUtils::CreateProgram(vShaderStr, fShaderStr);
}

void TriangleDrawer::SetWorldSize(int worldW, int worldH) {

}

void TriangleDrawer::Draw() {
    if (mProgramId == 0)
        return;

    //使用程序片段
    glUseProgram(mProgramId);

    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(1, 4, GL_FLOAT, GL_FALSE, 0, colors);
    glEnableVertexAttribArray(1);

    glDrawArrays(GL_TRIANGLES, 0, 3);
}

void TriangleDrawer::Release() {
    glDisableVertexAttribArray(0);
    glDisableVertexAttribArray(1);
    ShaderUtils::DeleteProgram(mProgramId);
}

void TriangleDrawer::LoadImage(ImageData *pImage) {

}
