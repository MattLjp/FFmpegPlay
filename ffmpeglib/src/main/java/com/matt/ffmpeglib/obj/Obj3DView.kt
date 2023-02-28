package com.matt.ffmpeglib.obj

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created by Liaojp on 20/2/2023.
 */
class Obj3DView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs), GLSurfaceView.Renderer {

    private val drawers = mutableListOf<Obj3DDrawer>()
    private val currMatrix by lazy {
        FloatArray(16).also {
            Matrix.setRotateM(it, 0, 0f, 1f, 0f, 0f)
        }
    }
    private val mvpMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val cameraMatrix = FloatArray(16)


    fun setObjPath(path: String) {
        ObjLoaderUtil.load(context.resources, path).forEach {
            drawers.add(Obj3DDrawer(context, it))
        }
        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //设置背景颜色
        GLES30.glClearColor(1f, 1f, 1f, 1f)
        //开启混合，即半透明
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // 启用深度测试
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        // 设置为打开背面剪裁
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        drawers.forEach {
            it.create()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 1f, 5f)
        Matrix.setLookAtM(
            cameraMatrix, 0, 0f, 0f, 2f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
        drawers.forEach {
            it.setWorldSize(width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)
//        Matrix.scaleM(currMatrix, 0, 5f, 5f, 5f)
        Matrix.rotateM(currMatrix, 0, moveX, 0f, 1f, 0f)
//        Matrix.rotateM(currMatrix, 0, moveY, 1f, 0f, 0f)
        Matrix.multiplyMM(mvpMatrix, 0, cameraMatrix, 0, currMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0)
        drawers.forEach {
            it.setMatrix(mvpMatrix, currMatrix)
            it.draw()
        }
    }

    //上次的触控位置Y坐标
    private var mPreviousY = 0f

    //上次的触控位置X坐标
    private var mPreviousX = 0f
    private var moveX = 0f
    private var moveY = 0f

    //触摸事件回调方法
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val y = e.y
        val x = e.x

        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                if (e.pointerCount == 1) {
                    //计算触控笔Y位移
                    moveY = y - mPreviousY
                    //计算触控笔X位移
                    moveX = x - mPreviousX
                } else {
                    Log.i("TAG", "onTouchEvent:0 x:" + e.getX(0) + " y:" + e.getY(0));
                    Log.i("TAG", "onTouchEvent:1 x:" + e.getX(1) + " y:" + e.getY(1));
                }
            }
            else -> {}
        }
        mPreviousY = y //记录触控笔位置
        mPreviousX = x //记录触控笔位置

        requestRender()
        return true
    }

    fun destroy() {
        drawers.forEach {
            it.release()
        }
        drawers.clear()
    }
}