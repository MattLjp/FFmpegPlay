package com.matt.ffmpeglib

import android.view.Surface

/**
 * Created by Liaojp on 21/2/2023.
 */
class NativeRender {

    external fun onInit(surface: Surface)

    external fun onUnInit()

    external fun setDrawerType(type: Int)

    external fun setImageData(format: Int, width: Int, height: Int, bytes: ByteArray?)

    external fun onSurfaceChanged(width: Int, height: Int)

    companion object {
        init {
            System.loadLibrary("ffmpeglib")
        }

        const val SAMPLE_TYPE_KEY_TRIANGLE = 100
        const val SAMPLE_TYPE_KEY_IMAGE = 101
        const val SAMPLE_TYPE_KEY_YUV_TEXTURE_MAP = 102


        const val IMAGE_FORMAT_RGBA = 0x01
        const val IMAGE_FORMAT_NV21 = 0x02
        const val IMAGE_FORMAT_NV12 = 0x03
        const val IMAGE_FORMAT_I420 = 0x04
        const val IMAGE_FORMAT_YUYV = 0x05
        const val IMAGE_FORMAT_GRAY = 0x06
        const val IMAGE_FORMAT_I444 = 0x07
        const val IMAGE_FORMAT_P010 = 0x08
    }
}