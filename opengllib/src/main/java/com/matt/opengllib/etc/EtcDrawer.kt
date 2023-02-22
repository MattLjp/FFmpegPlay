package com.matt.opengllib.etc

import android.content.Context
import android.opengl.ETC1
import android.opengl.ETC1Util
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.matt.opengllib.R
import com.matt.opengllib.drawer.IDrawer
import com.matt.opengllib.utils.TextureUtils
import com.matt.opengllib.utils.MatrixUtils
import com.matt.opengllib.utils.ResReadUtils
import com.matt.opengllib.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Liaojp on 16/2/2023.
 */
class EtcDrawer(private val context: Context) : IDrawer {

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
    private var textureIds: IntArray? = null
    private var uMatrixHandle = 0
    private var uTextureUnitHandle = 0
    private var uTextureAlphaHandle = 0

    private var worldWidth = 0
    private var worldHeight = 0

    private val mMatrix = FloatArray(16)
    private var emptyBuffer: ByteBuffer? = null
    private val pkmLoader: ZipPkmLoader by lazy {
        ZipPkmLoader(context)
    }

    private var isPlay = false
    private var time: Long = 0
    private var timeStep: Long = 0
    private var view: GLSurfaceView? = null
    var stateCallback: ((Int) -> Unit) = {}

    override fun create() {
        //编译
        val vertexShaderId =
            ShaderUtils.loadVertexShader(ResReadUtils.readResource(context, R.raw.etc_vertex_shader))
        val fragmentShaderId =
            ShaderUtils.loadFragmentShader(ResReadUtils.readResource(context, R.raw.etc_fragment_shader))

        //链接程序片段
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        //使用程序片段
        GLES30.glUseProgram(programId)

        textureIds = TextureUtils.createTextureID(2)
        uMatrixHandle = GLES30.glGetUniformLocation(programId, "uMatrix")
        uTextureUnitHandle = GLES30.glGetUniformLocation(programId, "uTextureUnit")
        uTextureAlphaHandle = GLES30.glGetUniformLocation(programId, "uTextureAlpha")
    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
        worldWidth = worldW
        worldHeight = worldH
        emptyBuffer = ByteBuffer.allocate(ETC1.getEncodedDataSize(worldW, worldH))

    }

    override fun draw() {
        time = System.currentTimeMillis()
        val startTime = System.currentTimeMillis()
        val s = System.currentTimeMillis() - startTime
        doDraw()
        if (isPlay) {
            if (s < timeStep) {
                try {
                    Thread.sleep(timeStep - s)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            view?.requestRender()
        } else {
            stateCallback(STOP)
        }
    }

    override fun release() {
        textureIds?.let { GLES30.glDeleteTextures(it.size, it, 0) }
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDeleteProgram(programId)
    }

    fun setAnimation(view: GLSurfaceView, path: String, timeStep: Int) {
        this.view = view
        this.timeStep = timeStep.toLong()
        pkmLoader.setZipPath(path)
    }

    fun start() {
        if (!isPlay) {
            stop()
            isPlay = true
            stateCallback(START)
            pkmLoader.open()
            view?.requestRender()
        }
    }

    fun isPlay(): Boolean {
        return isPlay
    }

    fun stop() {
        pkmLoader.close()
        isPlay = false
    }

    private fun doDraw() {
        val texture = pkmLoader.getNextTexture()
        val tAlpha = pkmLoader.getNextTexture()
        if (texture != null && tAlpha != null) {
            MatrixUtils.getMatrix(
                mMatrix,
                MatrixUtils.TYPE_CENTERINSIDE,
                texture.width,
                tAlpha.height,
                worldWidth,
                worldHeight
            )
            GLES30.glUniformMatrix4fv(uMatrixHandle, 1, false, mMatrix, 0)

            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)
            //启用顶点坐标属性
            GLES30.glEnableVertexAttribArray(0)

            GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)
            //启用纹理坐标属性
            GLES30.glEnableVertexAttribArray(1)

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds!![0])
            ETC1Util.loadTexture(GLES30.GL_TEXTURE_2D, 0, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_SHORT_5_6_5, texture)
            GLES30.glUniform1i(uTextureUnitHandle, 0)

            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds!![1])
            ETC1Util.loadTexture(GLES30.GL_TEXTURE_2D, 0, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_SHORT_5_6_5, tAlpha)
            GLES30.glUniform1i(uTextureAlphaHandle, 1)

            // 绘制
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, VERTEX_INDEX.size, GLES30.GL_UNSIGNED_SHORT, vertexIndexBuffer)
        } else {
            isPlay = false
        }
    }

    companion object {
        var START = 1
        var STOP = 2
    }

}