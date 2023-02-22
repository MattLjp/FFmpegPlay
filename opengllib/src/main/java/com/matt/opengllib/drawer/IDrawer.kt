package com.matt.opengllib.drawer

import android.graphics.SurfaceTexture
import android.opengl.GLES30

/**
 * Created by Liaojp on 16/2/2023.
 */
interface IDrawer {
    fun create()
    fun setVideoSize(videoW: Int, videoH: Int) {}
    fun setWorldSize(worldW: Int, worldH: Int) {
        GLES30.glViewport(0, 0, worldW, worldH)
    }

    fun setAlpha(alpha: Float) {}
    fun draw()
    fun getSurfaceTexture(cb: (st: SurfaceTexture) -> Unit) {}
    fun release()
}