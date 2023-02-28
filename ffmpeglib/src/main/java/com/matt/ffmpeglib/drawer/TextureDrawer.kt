package com.matt.ffmpeglib.drawer

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.Matrix
import com.matt.ffmpeglib.R
import com.matt.ffmpeglib.utils.ResReadUtils
import com.matt.ffmpeglib.utils.ShaderUtils
import com.matt.ffmpeglib.utils.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Liaojp on 2023/2/6.
 */
class TextureDrawer(private val context: Context, private val bitmap: Bitmap) : IDrawer {

    /**
     * 顶点坐标
     * (x,y,z)
     */
    private val POSITION_VERTEX = floatArrayOf(
        0f, 0f, 0f,  //顶点坐标V0
        1f, 1f, 0f,  //顶点坐标V1
        -1f, 1f, 0f,  //顶点坐标V2
        -1f, -1f, 0f,  //顶点坐标V3
        1f, -1f, 0f //顶点坐标V4
    )

    /**
     * 纹理坐标
     * (s,t)
     */
    private val TEX_VERTEX = floatArrayOf(
        0.5f, 0.5f,  //纹理坐标V0
        1f, 0f,  //纹理坐标V1
        0f, 0f,  //纹理坐标V2
        0f, 1.0f,  //纹理坐标V3
        1f, 1.0f //纹理坐标V4
    )

    /**
     * 索引
     */
    private val VERTEX_INDEX = shortArrayOf(
        0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
        0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
        0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
        0, 4, 1 //V0,V4,V1 三个顶点组成一个三角形
    )

    private val vertexBuffer by lazy {
        //分配内存空间,每个浮点型占4字节空间
        ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                //传入指定的坐标数据
                it.put(POSITION_VERTEX)
                it.position(0)
            }
    }

    private val texVertexBuffer by lazy {
        //分配内存空间,每个浮点型占4字节空间
        ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                //传入指定的坐标数据
                it.put(TEX_VERTEX)
                it.position(0)
            }
    }
    private val vertexIndexBuffer by lazy {
        //分配内存空间,每个Short占2字节空间
        ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer().also {
                //传入指定的坐标数据
                it.put(VERTEX_INDEX)
                it.position(0)
            }
    }
    private var programId = 0
    private var textureId: Int = 0
    private var uMatrixHandler = 0
    private var mMatrix = FloatArray(16)

    override fun create() {
        //编译
        val vertexShaderId =
            ShaderUtils.loadVertexShader(ResReadUtils.readResource(context, R.raw.texture_vertex_shader))
        val fragmentShaderId =
            ShaderUtils.loadFragmentShader(ResReadUtils.readResource(context, R.raw.texture_fragment_shader))

        //链接程序片段
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)

        uMatrixHandler = GLES30.glGetUniformLocation(programId, "u_Matrix")

        //使用程序片段
        GLES30.glUseProgram(programId)

        textureId = TextureUtils.createTextureID(bitmap)
    }

    override fun setVideoSize(videoW: Int, videoH: Int) {

    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
        val aspectRatio =
            if (worldW > worldH) worldW.toFloat() / worldH.toFloat() else worldH.toFloat() / worldW.toFloat()
        if (worldW > worldH) {
            //横屏
            Matrix.orthoM(mMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            //竖屏
            Matrix.orthoM(mMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }
    }

    override fun draw() {
        GLES30.glUniformMatrix4fv(uMatrixHandler, 1, false, mMatrix, 0)

        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        //启用顶点坐标属性
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)
        //启用纹理坐标属性
        GLES30.glEnableVertexAttribArray(1)

        //激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        // 绘制
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, VERTEX_INDEX.size, GLES30.GL_UNSIGNED_SHORT, vertexIndexBuffer)
    }

    override fun release() {
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDeleteProgram(programId)
    }


}