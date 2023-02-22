//
// Created by liaojp on 2023/2/22.
//

#include "TextureDrawer.h"

static char vShaderStr[] =
        "#version 300 es                            \n"
        "layout(location = 0) in vec4 a_position;   \n"
        "layout(location = 1) in vec2 a_texCoord;   \n"
        "out vec2 v_texCoord;                       \n"
        "void main()                                \n"
        "{                                          \n"
        "   gl_Position = a_position;               \n"
        "   v_texCoord = a_texCoord;                \n"
        "}                                          \n";

static char fShaderStr[] =
        "#version 300 es                                     \n"
        "precision mediump float;                            \n"
        "in vec2 v_texCoord;                                 \n"
        "out vec4 outColor;                                  \n"
        "uniform sampler2D s_TextureMap;                     \n"
        "void main()                                         \n"
        "{                                                   \n"
        "  outColor = texture(s_TextureMap, v_texCoord);     \n"
        "}                                                   \n";

static GLfloat verticesCoords[] = {
        -1.0f, 0.5f, 0.0f,
        -1.0f, -0.5f, 0.0f,
        1.0f, -0.5f, 0.0f,
        1.0f, 0.5f, 0.0f
};

static GLfloat textureCoords[] = {
        1.0f, 0.0f,
        1.0f, 1.0f,
        0.0f, 1.0f,
        0.0f, 0.0f,
};

static GLushort indices[] = {0, 1, 2, 0, 2, 3};

void TextureDrawer::Create() {
    mProgramId = ShaderUtils::CreateProgram(vShaderStr, fShaderStr);
    vTexCoordHandle = glGetUniformLocation(mProgramId, "v_texCoord");
    TextureUtils::CreateTextureID(&mTextureId);

}

void TextureDrawer::LoadImage(ImageData *pImage) {
    if (pImage) {
        NativeImageUtil::CopyNativeImage(&mRenderImage, pImage);
    }
}

void TextureDrawer::SetWorldSize(int worldW, int worldH) {

}

void TextureDrawer::Draw() {
    if (mProgramId == 0)return;
    glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    //使用程序片段
    glUseProgram(mProgramId);

    //upload RGBA image data
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, mRenderImage.width, mRenderImage.height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                 mRenderImage.ppPlane[0]);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);

    // Load the vertex position
    glVertexAttribPointer(0, 3, GL_FLOAT,GL_FALSE, 0, verticesCoords);
    // Load the texture coordinate
    glVertexAttribPointer(1, 2, GL_FLOAT,GL_FALSE, 0, textureCoords);

    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);

    // Bind the RGBA map
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureId);
    // Set the RGBA map sampler to texture unit to 0
    glUniform1i(vTexCoordHandle, 0);

    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices);
}

void TextureDrawer::Release() {

}
