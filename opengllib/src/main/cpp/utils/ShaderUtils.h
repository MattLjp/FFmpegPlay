//
// Created by liaojp on 2023/2/21.
//

#ifndef FFMPEGPLAY_SHADERUTILS_H
#define FFMPEGPLAY_SHADERUTILS_H


#include <malloc.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include "../utils/LogUtil.h"

class ShaderUtils {
public:
    /**
     * 创建着色器程序
     * @param pVertexShaderSource
     * @param pFragShaderSource
     * @return
     */
    static GLuint CreateProgram(const char *pVertexShaderSource, const char *pFragShaderSource) {
        GLuint program = 0;
        GLuint vertexShaderHandle = LoadShader(GL_VERTEX_SHADER, pVertexShaderSource);
        if (!vertexShaderHandle) return program;
        GLuint fragShaderHandle = LoadShader(GL_FRAGMENT_SHADER, pFragShaderSource);
        if (!fragShaderHandle) return program;
        program = glCreateProgram();
        if (program) {
            glAttachShader(program, vertexShaderHandle);
            CheckGLError("glAttachShader");
            glAttachShader(program, fragShaderHandle);
            CheckGLError("glAttachShader");
            glLinkProgram(program);
            GLint linkStatus = GL_FALSE;
            glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);

            glDetachShader(program, vertexShaderHandle);
            glDeleteShader(vertexShaderHandle);
            vertexShaderHandle = 0;
            glDetachShader(program, fragShaderHandle);
            glDeleteShader(fragShaderHandle);
            fragShaderHandle = 0;
            if (linkStatus != GL_TRUE) {
                GLint bufLength = 0;
                glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
                if (bufLength) {
                    char *buf = (char *) malloc((size_t) bufLength);
                    if (buf) {
                        glGetProgramInfoLog(program, bufLength, NULL, buf);
                        LOGE("ShaderUtils::CreateProgram Could not link program:%s", buf);
                        free(buf);
                    }
                }
                glDeleteProgram(program);
                program = 0;
            }
            LOGI("ShaderUtils::CreateProgram program = %d", program);
            return program;
        }

        return program;
    }

    static void DeleteProgram(GLuint &program) {
        LOGI("ShaderUtils::DeleteProgram");
        if (program) {
            glUseProgram(0);
            glDeleteProgram(program);
            program = 0;
        }
    }

private:
    static void CheckGLError(const char *pGLOperation) {
        for (GLint error = glGetError(); error; error = glGetError()) {
            LOGE("ShaderUtils::CheckGLError GL Operation %s() glError (0x%x)\n", pGLOperation, error);
        }

    }

    /**
     * 编译着色器
     * @param shaderType
     * @param pSource
     * @return
     */
    static GLuint LoadShader(GLenum shaderType, const char *pSource) {
        GLuint shader = 0;
        shader = glCreateShader(shaderType);
        if (shader) {
            glShaderSource(shader, 1, &pSource, NULL);
            glCompileShader(shader);
            GLint compiled = 0;
            glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
            if (!compiled) {
                GLint infoLen = 0;
                glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
                if (infoLen) {
                    char *buf = (char *) malloc((size_t) infoLen);
                    if (buf) {
                        glGetShaderInfoLog(shader, infoLen, NULL, buf);
                        LOGE("ShaderUtils::LoadShader Could not compile shader %d:\n%s\n", shaderType, buf);
                        free(buf);
                    }
                    glDeleteShader(shader);
                    shader = 0;
                }
            }
        }
        LOGI("ShaderUtils::LoadShader")
        return shader;
    }
};


#endif //FFMPEGPLAY_SHADERUTILS_H
