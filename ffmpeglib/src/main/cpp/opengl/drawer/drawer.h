//
// Created by Liaojp on 2023/2/24.
//

#ifndef FFMPEGPLAY_DRAWER_H
#define FFMPEGPLAY_DRAWER_H

#include <malloc.h>
#include "stdint.h"
#include <detail/type_mat.hpp>
#include <detail/type_mat4x4.hpp>
#include "../../utils/LogUtil.h"
#include "../../utils/ShaderUtils.h"
#include "../../utils/ImageData.h"
#include "../../utils/MatrixUtils.h"

extern "C" {
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
};

class Drawer {
private:
    const char *TAG = "Drawer";

    /**上下颠倒的顶点矩阵*/
    const GLfloat m_reserve_vertex_coors[8] = {
            -1.0f, 1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f
    };

    const GLfloat m_vertex_coors[8] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };

    const GLfloat m_texture_coors[8] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    GLuint m_texture_id = 0;

    GLint m_vertex_matrix_handler = -1;

    GLint m_texture_handler = -1;

    void CreateTextureId();

    void CreateProgram();

public:
    Drawer();

    virtual ~Drawer();

    void SetVideoSize(int width, int height) {
        m_video_width = width;
        m_video_height = height;
        MatrixUtils::GetMatrix(m_matrix, TYPE_CENTERINSIDE, m_video_width, m_video_height, m_view_width, m_view_height);
    }

    void SetWorldSize(int width, int height) {
        m_view_width = width;
        m_view_height = height;
    }

    void Render(uint8_t *data) {
        cst_data = data;
    }

    void Render(ImageData *image) {
        NativeImageUtil::CopyNativeImage(&imageData, image);
        MatrixUtils::GetMatrix(m_matrix, TYPE_CENTERINSIDE, image->width, image->height, m_view_width, m_view_height);
    }

    void Draw();

    /**
     * 释放OpenGL
     */
    void Release();

protected:
    glm::mat4 m_matrix = glm::mat4(1.0f);

    int m_video_width = 0;

    int m_video_height = 0;

    int m_view_width = 0;

    int m_view_height = 0;
    // 自定义用户数据，可用于存放画面数据
    uint8_t *cst_data = NULL;

    ImageData imageData;

    GLuint m_program_id = 0;

    void ActivateTexture(GLenum type = GL_TEXTURE_2D, GLuint texture = -1,
                         GLenum index = 0, int texture_handler = -1);

    virtual const GLchar *GetVertexShader() = 0;

    virtual const GLchar *GetFragmentShader() = 0;

    virtual void InitCstShaderHandler() = 0;

    virtual void BindTexture() = 0;

    virtual void PrepareDraw() = 0;

    virtual void DoDraw();

    virtual void Destroy() = 0;
};


#endif //OPENGL_DRAWER_H
