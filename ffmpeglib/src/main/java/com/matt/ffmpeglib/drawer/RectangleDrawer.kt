package com.matt.ffmpeglib.drawer

import android.opengl.GLES30
import android.opengl.Matrix
import com.matt.ffmpeglib.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Liaojp on 2023/1/12.
 */
class RectangleDrawer : IDrawer {
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

    private val vertexShaderCode =
        """
        #version 300 es 
        layout (location = 0) in vec4 vPosition;
        layout (location = 1) in vec4 aColor;
        uniform mat4 u_Matrix;
        out vec4 vColor;
        void main() { 
            gl_Position  = u_Matrix * vPosition;
            gl_PointSize = 10.0;
            vColor = aColor;
        }
        
        """.trimIndent()
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

    private val vertexPoints = floatArrayOf( //前两个是坐标,后三个是颜色RGB
        0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
        -0.5f, -0.5f, 1.0f, 1.0f, 1.0f,
        0.5f, -0.5f, 1.0f, 1.0f, 1.0f,
        0.5f, 0.5f, 1.0f, 1.0f, 1.0f,
        -0.5f, 0.5f, 1.0f, 1.0f, 1.0f,
        -0.5f, -0.5f, 1.0f, 1.0f, 1.0f,

        0.0f, 0.25f, 0.5f, 0.5f, 0.5f,
        0.0f, -0.25f, 0.5f, 0.5f, 0.5f
    )
    private var matrix = FloatArray(16)

    private val positionComponentCount = 2
    private val colorComponentCount = 3

    //OpenGL程序ID
    private var programId: Int = -1
    private var uMatrixHandler = 0
    private var aPositionHandler = 0
    private var aColorHandler = 0

    //定义的坐标数据中，每一行是5个数据，前两个表示坐标(x,y)，后三个表示颜色(r,g,b)，所以这里实际是 STRIDE = (2 + 3) x 4
    private val stride: Int = (positionComponentCount + colorComponentCount) * 4


    override fun create() {
        //编译
        val vertexShaderId = ShaderUtils.loadVertexShader(vertexShaderCode)
        val fragmentShaderId = ShaderUtils.loadFragmentShader(fragmentShaderCode)
        //链接程序片段
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        //在OpenGLES环境中使用程序片段
        GLES30.glUseProgram(programId)

        //获取顶点着色器的vPosition成员句柄
        aPositionHandler = GLES30.glGetAttribLocation(programId, "vPosition")
        //获取片元着色器的aColor成员的句柄
        aColorHandler = GLES30.glGetAttribLocation(programId, "aColor")
        uMatrixHandler = GLES30.glGetUniformLocation(programId, "u_Matrix")
    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
        val mProjectMatrix = FloatArray(16)
        val mViewMatrix = FloatArray(16)
        //相机和透视投影方式
        //计算宽高比
        val ratio = worldW.toFloat() / worldH
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(matrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    override fun draw() {
        GLES30.glUniformMatrix4fv(uMatrixHandler, 1, false, matrix, 0)

        vertexBuffer.position(0)
        GLES30.glVertexAttribPointer(
            aPositionHandler,
            positionComponentCount,
            GLES30.GL_FLOAT,
            false,
            stride,
            vertexBuffer
        )
        //启用顶点的句柄
        GLES30.glEnableVertexAttribArray(aPositionHandler)

        vertexBuffer.position(positionComponentCount)
        GLES30.glVertexAttribPointer(
            aColorHandler,
            colorComponentCount,
            GLES30.GL_FLOAT,
            false,
            stride,
            vertexBuffer
        )
        //启用颜色的句柄
        GLES30.glEnableVertexAttribArray(aColorHandler)

        //绘制矩形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 6)
        //绘制两个点
        GLES30.glDrawArrays(GLES30.GL_POINTS, 6, 2)
    }

    override fun release() {
        //禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(aPositionHandler)
        GLES30.glDisableVertexAttribArray(aColorHandler)
        GLES30.glDeleteProgram(programId)
    }
}