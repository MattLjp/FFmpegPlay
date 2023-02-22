package com.matt.ffmpeglib.media.extractor

import android.media.MediaFormat
import java.nio.ByteBuffer


/**
 * 音视频分离器定义
 *
 * @author Liao Jianpeng
 * @Date 2022/2/21
 * @email 329524627@qq.com
 * @Description :
 */
interface IExtractor {

    fun getFormat(): MediaFormat?

    /**
     * 读取音视频数据
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int

    /**
     * 获取当前帧时间
     */
    fun getCurrentTimestamp(): Long

    fun getSampleFlag(): Int

    /**
     * Seek到指定位置，并返回实际帧的时间戳
     */
    fun seek(timeUs: Long): Long

    /**
     * 停止读取数据
     */
    fun stop()
}