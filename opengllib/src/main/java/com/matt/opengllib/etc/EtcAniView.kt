package com.matt.opengllib.etc

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Liaojp on 19/2/2023.
 */
class EtcAniView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs), GLSurfaceView.Renderer {
    private val drawer: EtcDrawer = EtcDrawer(context)

    init {
        setEGLContextClientVersion(3)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setAnimation(path: String, timeStep: Int) {
        drawer.setAnimation(this, path, timeStep)
    }

    fun start() {
        drawer.start()
    }

    fun stop() {
        drawer.stop()
    }

    fun isPlay(): Boolean {
        return drawer.isPlay()
    }

    fun setStateChangeListener(callback: (Int) -> Unit) {
        drawer.stateCallback = callback
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //设置背景颜色
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        //开启混合，即半透明
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        drawer.create()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        drawer.setWorldSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        drawer.draw()
    }

    fun destroy() {
        drawer.release()
    }
}