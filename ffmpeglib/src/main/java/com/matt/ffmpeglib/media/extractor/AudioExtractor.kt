package com.matt.ffmpeglib.media.extractor

import android.media.MediaFormat
import java.nio.ByteBuffer


/**
 * 音频数据提取器
 *
 * @author Liao Jianpeng
 * @Date 2022/2/21
 * @email 329524627@qq.com
 * @Description :
 */
class AudioExtractor(path: String): IExtractor {

    private val mMediaExtractor = MMExtractor(path)

    override fun getFormat(): MediaFormat? {
        return mMediaExtractor.getAudioFormat()
    }

    override fun readBuffer(byteBuffer: ByteBuffer): Int {
        return mMediaExtractor.readBuffer(byteBuffer)
    }

    override fun getCurrentTimestamp(): Long {
        return mMediaExtractor.getCurrentTimestamp()
    }

    override fun getSampleFlag(): Int {
        return mMediaExtractor.getSampleFlag()
    }

    override fun seek(timeUs: Long): Long {
        return mMediaExtractor.seek(timeUs)
    }

    override fun stop() {
        mMediaExtractor.stop()
    }
}