package com.matt.opengllib.drawer

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import com.matt.opengllib.R
import com.matt.opengllib.utils.MatrixUtils
import com.matt.opengllib.utils.ResReadUtils
import com.matt.opengllib.utils.ShaderUtils
import com.matt.opengllib.utils.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Liaojp on 2023/2/6.
 */
class ImageDrawer(private val context: Context, private val bitmap: Bitmap) : IDrawer {

    /**
     * 顶点坐标
     * (x,y)
     */
    private val POSITION_VERTEX = floatArrayOf(
        0f, 0f,  //顶点坐标V0
        1f, 1f,  //顶点坐标V1
        -1f, 1f,  //顶点坐标V2
        -1f, -1f,  //顶点坐标V3
        1f, -1f //顶点坐标V4
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
        ByteBuffer.allocateDirect(POSITION_VERTEX.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().also {
            //传入指定的坐标数据
            it.put(POSITION_VERTEX)
            it.position(0)
        }
    }

    private val texVertexBuffer by lazy {
        //分配内存空间,每个浮点型占4字节空间
        ByteBuffer.allocateDirect(TEX_VERTEX.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().also {
            //传入指定的坐标数据
            it.put(TEX_VERTEX)
            it.position(0)
        }
    }
    private val vertexIndexBuffer by lazy {
        //分配内存空间,每个Short占2字节空间
        ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().also {
            //传入指定的坐标数据
            it.put(VERTEX_INDEX)
            it.position(0)
        }
    }
    private var programId = 0
    private var textureId: Int = 0
    private val mMatrix = FloatArray(16)
    private var glHTexture = 0
    private var glHMatrix = 0
    private var hIsHalf = 0
    private var glHUxy = 0
    private var hChangeType = 0
    private var hChangeColor = 0

    private var imageWidth = 0
    private var imageHeight = 0
    private var uXY = 0f
    private var isHalf = false
    private var type = 0
    private var data = floatArrayOf(0.0f, 0.0f, 0.0f)

    fun setData(isHalf: Boolean, type: Int, data: FloatArray) {
        this.isHalf = isHalf
        this.type = type
        this.data = data
    }


    override fun create() {
        //编译
        val vertexShaderId = ShaderUtils.loadVertexShader(ResReadUtils.readResource(context, R.raw.image_vertex_shader))
        val fragmentShaderId =
            ShaderUtils.loadFragmentShader(ResReadUtils.readResource(context, R.raw.image_fragment_shader))

        //链接程序片段
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)

        glHTexture = GLES30.glGetUniformLocation(programId, "vTexture")
        glHMatrix = GLES30.glGetUniformLocation(programId, "vMatrix")
        hIsHalf = GLES30.glGetUniformLocation(programId, "vIsHalf")
        glHUxy = GLES30.glGetUniformLocation(programId, "uXY")
        hChangeType = GLES30.glGetUniformLocation(programId, "vChangeType")
        hChangeColor = GLES30.glGetUniformLocation(programId, "vChangeColor")

        //使用程序片段
        GLES30.glUseProgram(programId)

        textureId = TextureUtils.createTextureID(bitmap)
    }


    override fun setWorldSize(worldW: Int, worldH: Int) {
        uXY = worldW / worldH.toFloat()
        imageWidth = bitmap.width
        imageHeight = bitmap.height
        MatrixUtils.getMatrix(mMatrix, MatrixUtils.TYPE_CENTERINSIDE, imageWidth, imageHeight, worldW, worldH)
    }

    override fun setVideoSize(videoW: Int, videoH: Int) {
        imageWidth = videoW
        imageHeight = videoH
    }


    override fun draw() {
        GLES30.glUniform1i(hIsHalf, if (isHalf) 1 else 0)
        GLES30.glUniform1f(glHUxy, uXY)
        GLES30.glUniformMatrix4fv(glHMatrix, 1, false, mMatrix, 0)

        GLES30.glUniform1i(hChangeType, type)
        GLES30.glUniform3fv(hChangeColor, 1, data, 0)

        //启用顶点坐标属性
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        //启用纹理坐标属性
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)
        //激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(glHTexture, 0)
        // 绘制
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, VERTEX_INDEX.size, GLES30.GL_UNSIGNED_SHORT, vertexIndexBuffer)

    }

    override fun release() {
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDeleteProgram(programId)
    }


}