package com.matt.ffmpeglib.obj

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import com.matt.ffmpeglib.R
import com.matt.ffmpeglib.drawer.IDrawer
import com.matt.ffmpeglib.utils.ResReadUtils
import com.matt.ffmpeglib.utils.ShaderUtils
import com.matt.ffmpeglib.utils.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by Liaojp on 20/2/2023.
 */
class Obj3DDrawer(private val context: Context, private val objInfo: ObjInfo) : IDrawer {

    private var programId = 0

    private var muMVPMatrixHandle = 0

    //位置、旋转变换矩阵
    private var muMMatrixHandle = 0

    //光源位置属性引用
    private var maLightLocationHandle = 0

    //光照颜色位置属性引用
    private var maLightColorHandle = 0

    //摄像机位置属性引用
    private var maCameraHandle = 0

    //透明位置属性引用
    private var maAlpha = 0


    private val textureId by lazy {
        val bmpPath = objInfo.mtlData?.Kd_Texture
        if (bmpPath.isNullOrEmpty()) {
            0
        } else {
            val bmp = BitmapFactory.decodeStream(context.assets.open(bmpPath))
            TextureUtils.createTextureID(bmp)
        }

    }

    private val vCount = (objInfo.aVertices?.size ?: 0) / 3

    //顶点坐标数据缓冲
    private val mVertexBuffer by lazy {
        val data = objInfo.aVertices ?: floatArrayOf()
        ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(data)
                position(0)
            }
    }

    //顶点法向量数据缓冲
    private val mNormalBuffer by lazy {
        val data = objInfo.aNormals ?: floatArrayOf()
        ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(data)
                position(0)
            }
    }

    //顶点纹理坐标数据缓冲
    private val mTexCoorBuffer by lazy {
        val data = objInfo.aTexCoords ?: floatArrayOf()
        ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(data)
                position(0)
            }
    }

    /**
     * 灯光位置
     */
    private val lightLocalBuffer by lazy {
        ByteBuffer.allocateDirect(3 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(floatArrayOf(1000f, 1000f, 1000f))
                position(0)
            }
    }

    private val cameraBuffer by lazy {
        ByteBuffer.allocateDirect(3 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(floatArrayOf(10f, 10f, 30f))
                position(0)
            }
    }

    override fun create() {
        //编译
        val fragmentShaderId =
            ShaderUtils.loadFragmentShader(ResReadUtils.readResource(context, R.raw.obj_fragment_shader))

        val vertexShaderId =
            ShaderUtils.loadVertexShader(ResReadUtils.readResource(context, R.raw.obj_vertex_shader))

        //链接程序片段
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)

        //获取程序中总变换矩阵引用
        muMVPMatrixHandle = GLES30.glGetUniformLocation(programId, "u_MVPMatrix")
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES30.glGetUniformLocation(programId, "u_ModelMatrix")
        //获取程序中光源位置引用
        maLightLocationHandle = GLES30.glGetUniformLocation(programId, "lightPos")
        //获取程序中光照颜色位置引用
//        maLightColorHandle = GLES30.glGetUniformLocation(programId, "lightColor")
        //获取程序中摄像机位置引用
        maCameraHandle = GLES30.glGetUniformLocation(programId, "uCamera")
        //透明位置属性引用
        maAlpha = GLES30.glGetUniformLocation(programId, "alpha")
    }

    fun setMatrix(mvpMatrix: FloatArray, currMatrix: FloatArray) {
        //使用着色器程序
        GLES30.glUseProgram(programId)

        //将最终变换矩阵传入着色器程序
        GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mvpMatrix, 0)
        //将位置、旋转变换矩阵传入着色器程序
        GLES30.glUniformMatrix4fv(muMMatrixHandle, 1, false, currMatrix, 0)
    }

    override fun draw() {
        //制定使用着色器程序
        GLES30.glUseProgram(programId)

        //将光源位置传入着色器程序
        GLES30.glUniform3fv(maLightLocationHandle, 1, lightLocalBuffer)
        //将摄像机位置传入着色器程序
        GLES30.glUniform3fv(maCameraHandle, 1, cameraBuffer)
        // 将顶点位置数据传入渲染管线
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 3 * 4, mVertexBuffer)
        GLES30.glEnableVertexAttribArray(0)
        //将顶点法向量数据传入渲染管线
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 3 * 4, mNormalBuffer)
        GLES30.glEnableVertexAttribArray(1)
        //为画笔指定顶点纹理坐标数据
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 2 * 4, mTexCoorBuffer)
        GLES30.glEnableVertexAttribArray(2)
        // 材质alpha
        GLES30.glUniform1f(maAlpha, objInfo.mtlData?.alpha ?: 1f)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        //绘制加载的物体
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount)
    }

    override fun release() {
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDisableVertexAttribArray(2)
        GLES30.glDeleteProgram(programId)
    }

}