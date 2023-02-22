package com.matt.opengllib.drawer

import android.opengl.GLES30
import com.matt.opengllib.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Liaojp on 2023/1/12.
 */
class TriangleDrawer : IDrawer {
    private val vertexBuffer by lazy {
        //分配内存空间,每个浮点型占4字节空间
        ByteBuffer.allocateDirect(vertexPoints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                //传入指定的坐标数据
                it.put(vertexPoints)
                it.position(0)
            }
    }
    private val vertexPoints = floatArrayOf(
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    )
    private val vertexShaderCode =
        """
        #version 300 es 
        layout (location = 0) in vec4 vPosition;
        layout (location = 1) in vec4 aColor;
        out vec4 vColor;
        void main() { 
            gl_Position  = vPosition;
            gl_PointSize = 10.0;
            vColor = aColor;
        }
        
        """.trimIndent()


    private val colorBuffer by lazy {
        ByteBuffer.allocateDirect(color.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                it.put(color)
                it.position(0)
            }
    }
    private val color = floatArrayOf(
        0.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f
    )
    private val fragmentShaderCode =
        """
        #version 300 es 
        precision mediump float;
        in vec4 vColor;
        out vec4 fragColor;
        void main() { 
            fragColor = vColor; 
        }
        
        """.trimIndent()


    //顶点属性大小，即3位(xyz)为一个点
    private val vertexSize = 3

    //顶点个数
    private val vertexCount: Int = vertexPoints.size / vertexSize

    //顶点之间的偏移量
    private val vertexStride: Int = vertexSize * 4 // 每个顶点四个字节

    //OpenGL程序ID
    private var programId: Int = -1

    override fun create() {
        //编译
        val vertexShaderId = ShaderUtils.loadVertexShader(vertexShaderCode)
        val fragmentShaderId = ShaderUtils.loadFragmentShader(fragmentShaderCode)
        //链接程序片段
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        //在OpenGLES环境中使用程序片段
        GLES30.glUseProgram(programId)
    }
    

    override fun draw() {
        //准备坐标数据
        //第一个参数指定从索引0开始取数据，与顶点着色器中layout(location=0)对应
        //第二个参数指定顶点属性大小
        //第三个参数指定数据类型
        //第四个参数定义是否希望数据被标准化（归一化），只表示方向不表示大小
        //第五个参数是步长（Stride），指定在连续的顶点属性之间的间隔。传0和传[vertexStride]效果相同，如果传1取值方式为0123、1234、2345…
        GLES30.glVertexAttribPointer(0, vertexSize, GLES30.GL_FLOAT, false, vertexStride, vertexBuffer)
        //启用顶点的句柄
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 0, colorBuffer)
        GLES30.glEnableVertexAttribArray(1)

        //绘制三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
    }


    override fun release() {
        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDeleteProgram(programId)
    }
}