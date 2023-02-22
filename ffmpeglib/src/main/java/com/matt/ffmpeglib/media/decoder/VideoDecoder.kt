package com.matt.ffmpeglib.media.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import com.matt.ffmpeglib.media.extractor.IExtractor
import com.matt.ffmpeglib.media.extractor.VideoExtractor
import java.nio.ByteBuffer


/**
 * 视频解码器
 *
 * @author Liao Jianpeng
 * @Date 2022/2/21
 * @email 329524627@qq.com
 * @Description :
 */
class VideoDecoder(path: String, private val surface: Surface?) :
    BaseDecoder(path) {
    private val TAG = "VideoDecoder"

    override fun check(): Boolean {
        if (surface == null) {
            Log.e(TAG, "Surface为空")
            stateListener?.decoderError(this, "显示器为空")
            return false
        }
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return VideoExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        codec.configure(format, surface, null, 0)
        return true
    }

    override fun initRender(): Boolean {
        return true
    }

    override fun render(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
    }

    override fun doneDecode() {
    }
}