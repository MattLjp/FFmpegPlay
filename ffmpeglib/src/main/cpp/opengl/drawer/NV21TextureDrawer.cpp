//
// Created by liaojp on 2023/2/27.
//

#include "NV21TextureDrawer.h"

const GLchar *NV21TextureDrawer::GetVertexShader() {
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

const GLchar *NV21TextureDrawer::GetFragmentShader() {
    const GLchar *shader = "#version 300 es                                     \n"
                           "precision mediump float;                            \n"
                           "in vec2 vCoordinate;                                \n"
                           "out vec4 outColor;                                  \n"
                           "uniform sampler2D y_texture;                        \n"
                           "uniform sampler2D uv_texture;                       \n"
                           "void main(){                                        \n"
                           "    vec3 yuv;\n"
                           "    yuv.x = texture(y_texture, vCoordinate).r;  \n"
                           "    yuv.y = texture(uv_texture, vCoordinate).a-0.5;\n"
                           "    yuv.z = texture(uv_texture, vCoordinate).r-0.5;\n"
                           "    highp vec3 rgb =mat3(1.0,1.0,1.0,\n"
                           "                   0.0,-0.344,1.770,\n"
                           "                   1.403,-0.714,0.0) * yuv; \n"
                           "    outColor = vec4(rgb, 1);\n"
                           "}";

    return shader;
}

void NV21TextureDrawer::InitCstShaderHandler() {
    y_texture_handler = glGetUniformLocation(m_program_id, "y_texture");
    uv_texture_handler = glGetUniformLocation(m_program_id, "uv_texture");
}

void NV21TextureDrawer::BindTexture() {
//    glGenTextures(2, textureIds);
//    ActivateTexture(GL_TEXTURE_2D, textureIds[0], 0, y_texture_handler);
//    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, imageData.width, imageData.height, 0, GL_LUMINANCE,
//                 GL_UNSIGNED_BYTE,
//                 imageData.ppPlane[0]);
//    ActivateTexture(GL_TEXTURE_2D, textureIds[1], 1, uv_texture_handler);
//    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, imageData.width >> 1, imageData.height >> 1, 0, GL_LUMINANCE,
//                 GL_UNSIGNED_BYTE,
//                 imageData.ppPlane[1]);
    glGenTextures(2, textureIds);
    //upload Y plane data
    glBindTexture(GL_TEXTURE_2D, textureIds[0]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, imageData.width, imageData.height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, imageData.ppPlane[0]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);

    //update UV plane data
    glBindTexture(GL_TEXTURE_2D, textureIds[1]);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, imageData.width >> 1, imageData.height >> 1, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, imageData.ppPlane[1]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
}

void NV21TextureDrawer::PrepareDraw() {
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureIds[0]);
    glUniform1i(y_texture_handler, 0);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D,  textureIds[1]);
    glUniform1i(uv_texture_handler, 1);
}

void NV21TextureDrawer::Destroy() {
    glDeleteTextures(2, textureIds);
}
