package com.matt.ffmpeglib.media

import android.view.Surface
import com.matt.ffmpeglib.media.decoder.AudioDecoder
import com.matt.ffmpeglib.media.decoder.IDecoderStateListener
import com.matt.ffmpeglib.media.decoder.VideoDecoder

/**
 * @ Author : 廖健鹏
 * @ Date : 2022/2/21
 * @ e-mail : 329524627@qq.com
 * @ Description :
 */
class ANativePlayer {
    private var audioDecoder: AudioDecoder? = null
    private var videoDecoder: VideoDecoder? = null
    private var stateListener: IDecoderStateListener? = null

    fun createPlay(
        url: String,
        surface: Surface,
        decoderType: RenderType = RenderType.ANativeRender,
    ) {
        audioDecoder = AudioDecoder(url)
        videoDecoder = VideoDecoder(url, surface)
        videoDecoder?.stateListener = stateListener
    }


    fun play() {
        audioDecoder?.play()
        videoDecoder?.play()
    }

    fun pause() {
        audioDecoder?.pause()
        videoDecoder?.pause()
    }

    fun stop() {
        audioDecoder?.stop()
        videoDecoder?.stop()
    }

    fun seekToPosition(int: Int) {
        videoDecoder?.seekTo(int)
        audioDecoder?.seekTo(int)

    }

    fun setStateListener(listener: IDecoderStateListener) {
        stateListener = listener
    }

    enum class RenderType {
        ANativeRender
    }
}