package com.matt.ffmpeglib.media.decoder


/**
 * 解码状态回调接口
 *
 * @author Liao Jianpeng
 * @Date 2022/2/21
 * @email 329524627@qq.com
 * @Description :
 */
interface IDecoderStateListener {
    fun decoderError(decodeJob: BaseDecoder?, msg: String)
    fun decoderReady(decodeJob: BaseDecoder?, duration: Long)
    fun decoderPlay(decodeJob: BaseDecoder?)
    fun decoderPause(decodeJob: BaseDecoder?)
    fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame, time: Long)
    fun decoderFinish(decodeJob: BaseDecoder?)
    fun decoderStop(decodeJob: BaseDecoder?)

}