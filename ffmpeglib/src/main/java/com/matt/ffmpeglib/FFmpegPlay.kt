package com.matt.ffmpeglib

import android.view.Surface

/**
 * Created by Liaojp on 2023/1/6.
 */
class FFmpegPlay {
    private var playerHandle = -1L
    private var mEventCallback: EventCallback? = null

    fun createPlay(url: String, surface: Surface) {
        playerHandle = create(url, surface)
    }

    fun play() {
        play(playerHandle)
    }

    fun pause() {
        pause(playerHandle)
    }

    fun stop() {
        stop(playerHandle)
        playerHandle = -1
    }

    fun seekToPosition(progress: Int) {
        seekToPosition(playerHandle, progress)
    }


    private external fun create(url: String, surface: Surface): Long

    private external fun play(playerHandle: Long)

    private external fun pause(playerHandle: Long)

    private external fun stop(playerHandle: Long)

    private external fun seekToPosition(playerHandle: Long, progress: Int)

    private fun playerEventCallback(msgType: Int, msgValue: Int) {
        mEventCallback?.onPlayerEvent(msgType, msgValue)
    }

    fun addEventCallback(callback: EventCallback) {
        mEventCallback = callback
    }

    interface EventCallback {
        fun onPlayerEvent(msgType: Int, msgValue: Int)
    }


    companion object {
        const val MSG_DECODER_INIT_ERROR = 0
        const val MSG_DECODER_PREPARE = 1
        const val MSG_DECODER_PLAY = 2
        const val MSG_DECODER_PAUSE = 3
        const val MSG_DECODER_FINISH = 4
        const val MSG_DECODER_STOP = 5
        const val MSG_DECODER_TIME = 6

        init {
            System.loadLibrary("ffmpeglib")
        }

        external fun getFFmpegVersion(): String
    }
}