package com.matt.opengllib.render

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Liaojp on 2023/2/21.
 */
class MyGLRender : GLSurfaceView.Renderer {
    private val nativeRender by lazy { NativeRender() }

    init {
        nativeRender.onInit()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        nativeRender.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        nativeRender.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        nativeRender.onDrawFrame()
    }


    fun setDrawerType(type: Int) {
        nativeRender.setDrawerType(type)
    }

    fun setImageData(bitmap: Bitmap) {
        val byteArray = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(byteArray)
        nativeRender.setImageData(0, bitmap.width, bitmap.height, byteArray.array())
    }

    fun destroy() {
        nativeRender.onUnInit()
    }

}