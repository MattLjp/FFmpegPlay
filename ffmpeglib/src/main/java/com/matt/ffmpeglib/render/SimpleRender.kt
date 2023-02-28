package com.matt.ffmpeglib.render

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.matt.ffmpeglib.drawer.IDrawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Liaojp on 16/2/2023.
 */
class SimpleRender : GLSurfaceView.Renderer {
    private val drawers = mutableListOf<IDrawer>()

    private var width = 0
    private var height = 0
    private var config: EGLConfig? = null
    private var refreshFlag = false


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        this.config = config
        //设置背景颜色
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        //开启混合，即半透明
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        drawers.forEach {
            it.create()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES30.glViewport(0, 0, width, height)
        drawers.forEach {
            it.setWorldSize(width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        if (refreshFlag) {
            refreshFlag = false
            drawers.forEach {
                it.release()
            }
            onSurfaceCreated(gl, config)
            onSurfaceChanged(gl, width, height)
            onDrawFrame(gl)
        } else {
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
            drawers.forEach {
                it.draw()
            }
        }
    }

    fun setDrawer(drawer: IDrawer) {
        drawers.clear()
        drawers.add(drawer)
        refreshFlag = true
    }

    fun addDrawer(drawer: IDrawer) {
        drawers.add(drawer)
    }

    fun destroy() {
        drawers.forEach {
            it.release()
        }
        drawers.clear()
    }
}