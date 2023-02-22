package com.matt.opengllib.render

/**
 * Created by Liaojp on 21/2/2023.
 */
class NativeRender {

    external fun onInit()

    external fun onUnInit()

    external fun setDrawerType(type: Int)

    external fun setImageData(format: Int, width: Int, height: Int, bytes: ByteArray?)

    external fun onSurfaceCreated()

    external fun onSurfaceChanged(width: Int, height: Int)

    external fun onDrawFrame()

    companion object {
        init {
            System.loadLibrary("opengllib")
        }

        const val SAMPLE_TYPE_KEY_TRIANGLE = 100
        const val SAMPLE_TYPE_KEY_IMAGE = 101
    }
}