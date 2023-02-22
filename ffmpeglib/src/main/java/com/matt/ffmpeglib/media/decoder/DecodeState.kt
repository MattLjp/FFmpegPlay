package com.matt.ffmpeglib.media.decoder

/**
 * 解码状态
 *
 * @author Liao Jianpeng
 * @Date 2022/2/21
 * @email 329524627@qq.com
 * @Description :
 */
enum class DecodeState {
    /**解码器释放*/
    STOP,

    /**准备好状态*/
    READY,

    /**解码中*/
    DECODING,

    /**解码暂停*/
    PAUSE,

    /**解码完成*/
    FINISH,
}
