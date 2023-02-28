//
// Created by Liaojp on 2023/2/24.
//


#include "Drawer.h"


Drawer::Drawer() {

}

Drawer::~Drawer() {
}
void Drawer::Draw() {
    CreateTextureId();
    CreateProgram();
    BindTexture();
    PrepareDraw();
    DoDraw();
}


void Drawer::DoDraw() {
    //启用顶点的句柄
    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);
    //设置着色器参数
    glUniformMatrix4fv(m_vertex_matrix_handler, 1, GL_FALSE, &m_matrix[0][0]);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, m_vertex_coors);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 0, m_texture_coors);
    //开始绘制
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(0);
    glDisableVertexAttribArray(1);
}

void Drawer::CreateTextureId() {
    if (m_texture_id == 0) {
        glGenTextures(1, &m_texture_id);
        LOGI(TAG, "Create texture id : %d, %x", m_texture_id, glGetError())
    }
}

void Drawer::CreateProgram() {
    if (m_program_id == 0) {
        //创建一个空的OpenGLES程序，注意：需要在OpenGL渲染线程中创建，否则无法渲染
        m_program_id = ShaderUtils::CreateProgram(GetVertexShader(), GetFragmentShader());
        m_vertex_matrix_handler = glGetUniformLocation(m_program_id, "uMatrix");
        m_texture_handler = glGetUniformLocation(m_program_id, "uTexture");

        InitCstShaderHandler();
    }
    //使用OpenGL程序
    if (m_program_id != 0) {
        glUseProgram(m_program_id);
    }
}

void Drawer::ActivateTexture(GLenum type, GLuint texture, GLenum index, int texture_handler) {
    if (texture == -1) texture = m_texture_id;
    if (texture_handler == -1) texture_handler = m_texture_handler;
    //激活指定纹理单元
    glActiveTexture(GL_TEXTURE0 + index);
    //绑定纹理ID到纹理单元
    glBindTexture(type, texture);
    //将活动的纹理单元传递到着色器里面
    glUniform1i(texture_handler, index);
    //配置边缘过渡参数
    glTexParameterf(type, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
}

void Drawer::Release() {
    glBindTexture(GL_TEXTURE_2D, 0);
    glDeleteTextures(1, &m_texture_id);
    glDeleteProgram(m_program_id);
    m_program_id = 0;
    m_texture_id = 0;
    Destroy();
}

