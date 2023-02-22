package com.matt.opengllib.drawer

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import com.matt.opengllib.R
import com.matt.opengllib.utils.TextureUtils
import com.matt.opengllib.utils.MatrixUtils
import com.matt.opengllib.utils.ResReadUtils
import com.matt.opengllib.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Liaojp on 15/2/2023.
 */
class FBODrawer(private val context: Context, private val bitmap: Bitmap) : IDrawer {
    private var imageBuffer: ByteBuffer? = null
    private val fFrame = IntArray(1)
    private val fRender = IntArray(1)
    private val fTexture = IntArray(2)
    var callback: ((ByteBuffer, Int, Int) -> Unit) = { _, _, _ -> }


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

    private var program = 0
    private var matrix = FloatArray(16)
    private val previewMatrix = FloatArray(16)
    private var saveMatrix = MatrixUtils.flip(MatrixUtils.getOriginalMatrix(), false, true)
    private var glHTexture = 0
    private var glHMatrix = 0
    private var hIsHalf = 0
    private var glHUxy = 0
    private var hChangeType = 0
    private var hChangeColor = 0

    private var imageWidth = 0
    private var imageHeight = 0
    private var worldWidth = 0
    private var worldHeight = 0

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
        val vertexShaderId =
            ShaderUtils.loadVertexShader(ResReadUtils.readResource(context, R.raw.image_vertex_shader))
        val fragmentShaderId =
            ShaderUtils.loadFragmentShader(ResReadUtils.readResource(context, R.raw.image_fragment_shader))

        //链接程序片段
        program = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)

        //使用程序片段
        GLES30.glUseProgram(program)

        glHTexture = GLES30.glGetUniformLocation(program, "vTexture")
        glHMatrix = GLES30.glGetUniformLocation(program, "vMatrix")
        hIsHalf = GLES30.glGetUniformLocation(program, "vIsHalf")
        glHUxy = GLES30.glGetUniformLocation(program, "uXY")
        hChangeType = GLES30.glGetUniformLocation(program, "vChangeType")
        hChangeColor = GLES30.glGetUniformLocation(program, "vChangeColor")
    }

    override fun setWorldSize(worldW: Int, worldH: Int) {
        imageWidth = bitmap.width
        imageHeight = bitmap.height
        worldWidth = worldW
        worldHeight = worldH
        createTextureIds()
        MatrixUtils.getMatrix(
            previewMatrix,
            MatrixUtils.TYPE_CENTERINSIDE,
            imageWidth,
            imageHeight,
            worldWidth,
            worldHeight
        )
//        saveMatrix = previewMatrix.clone()
//        MatrixUtils.flip(saveMatrix, true, false)
    }

    override fun draw() {
        bindFBO()
        GLES30.glViewport(0, 0, imageWidth, imageHeight)
        matrix = saveMatrix
        doDraw()
        GLES30.glReadPixels(0, 0, imageWidth, imageHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, imageBuffer)
        callback.invoke(imageBuffer!!, imageWidth, imageHeight)
        unbindFBO()
        GLES30.glViewport(0, 0, worldWidth, worldHeight)
        matrix = previewMatrix
        doDraw()
    }

    private fun doDraw() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUniformMatrix4fv(glHMatrix, 1, false, matrix, 0)

        GLES30.glUniform1i(hIsHalf, if (isHalf) 1 else 0)
        GLES30.glUniform1f(glHUxy, uXY)

        GLES30.glUniform1i(hChangeType, type)
        GLES30.glUniform3fv(hChangeColor, 1, data, 0)

        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        //启用顶点坐标属性
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)
        //启用纹理坐标属性
        GLES30.glEnableVertexAttribArray(1)

        //激活纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fTexture[0])
        GLES30.glUniform1i(glHTexture, 0)
        // 绘制
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, VERTEX_INDEX.size, GLES30.GL_UNSIGNED_SHORT, vertexIndexBuffer)
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
    }

    private fun createTextureIds() {
        //生成Frame Buffer
        GLES30.glGenFramebuffers(1, fFrame, 0)
        //生成Render Buffer
        GLES30.glGenRenderbuffers(1, fRender, 0)
        //绑定Render Buffer
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, fRender[0])
        //设置为深度的Render Buffer，并传入大小
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, imageWidth, imageHeight)

        // 生成2个Texture，一个是作为数据源的texture，另外一个是用来作为输出图像的texture
        GLES30.glGenTextures(2, fTexture, 0)
        repeat(2) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fTexture[it])
            TextureUtils.setTexParameter()
            if (it == 0) {
                GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, bitmap, 0)
            } else {
                // 根据颜色参数，宽高等信息，为上面的纹理ID，生成一个2D纹理
                GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, imageWidth, imageHeight, 0,
                    GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null
                )
            }
        }
        // 取消绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        imageBuffer = ByteBuffer.allocate(imageWidth * imageHeight * 4)
    }

    private fun bindFBO() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fFrame[0])
        //为FrameBuffer挂载Texture[1]来存储颜色
        //在 2D 中，通常只用到了颜色附着,另外两种附着通常在 3D 渲染中使用(下面的深度附着可省略)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D, fTexture[1], 0
        )
        //为FrameBuffer挂载fRender[0]来存储深度
        GLES30.glFramebufferRenderbuffer(
            GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,
            GLES30.GL_RENDERBUFFER, fRender[0]
        )
    }

    private fun unbindFBO() {
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    override fun release() {
        GLES30.glDeleteTextures(2, fTexture, 0)
        GLES30.glDeleteRenderbuffers(1, fRender, 0)
        GLES30.glDeleteFramebuffers(1, fFrame, 0)
    }

}