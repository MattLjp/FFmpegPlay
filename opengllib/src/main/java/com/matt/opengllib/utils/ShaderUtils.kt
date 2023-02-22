package com.matt.opengllib.utils

import android.opengl.GLES30
import android.util.Log


/**
 * Created by Liaojp on 2023/1/12.
 */
object ShaderUtils {
    private const val TAG = "ShaderUtils"


    /**
     * 编译顶点着色器
     * @param shaderCode String
     * @return Int
     */
    fun loadVertexShader(shaderCode: String) = loadShader(GLES30.GL_VERTEX_SHADER, shaderCode)

    /**
     * 编译片段着色器
     * @param shaderCode String
     * @return Int
     */
    fun loadFragmentShader(shaderCode: String) = loadShader(GLES30.GL_FRAGMENT_SHADER, shaderCode)

    /**
     * 编译着色器
     * @param type Int
     * @param shaderCode String
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        //创建一个着色器
        val shaderId = GLES30.glCreateShader(type)
        if (shaderId != 0) {
            //加载到着色器
            GLES30.glShaderSource(shaderId, shaderCode)
            //编译着色器
            GLES30.glCompileShader(shaderId)
            //检测状态
            val compileStatus = IntArray(1)
            GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
            //创建失败
            if (compileStatus[0] == 0) {
                val logInfo = GLES30.glGetShaderInfoLog(shaderId)
                Log.e(TAG, "加载着色器失败: $logInfo")
                GLES30.glDeleteShader(shaderId)
                return 0
            }
            return shaderId
        }
        Log.e(TAG, "创建着色器失败")
        return 0
    }

    /**
     * 创建着色器程序
     * @param vertexShaderId Int
     * @param fragmentShaderId Int
     */
    fun createProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val programId = GLES30.glCreateProgram()
        if (programId != 0) {
            //将顶点着色器加入到程序
            GLES30.glAttachShader(programId, vertexShaderId)
            //将片元着色器加入到程序中
            GLES30.glAttachShader(programId, fragmentShaderId)
            //链接着色器程序
            GLES30.glLinkProgram(programId)
            //检测状态
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
            //创建失败
            if (linkStatus[0] == 0) {
                val logInfo = GLES30.glGetProgramInfoLog(programId)
                Log.e(TAG, "链接着色器程序失败: $logInfo")
                GLES30.glDeleteProgram(programId)
                return 0
            }
            return programId
        }
        Log.e(TAG, "创建程序失败")
        return 0
    }





}