package com.matt.opengllib.render

import android.content.Context
import android.opengl.GLSurfaceView

/**
 * Created by Liaojp on 2023/2/22.
 */
class MyGLSurfaceView @JvmOverloads constructor(
    context: Context, myGLRender: MyGLRender,
) : GLSurfaceView(context, null) {

    init {
        setEGLContextClientVersion(3)
        setRenderer(myGLRender)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

}