package com.matt.ffmpeglib.media.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.matt.ffmpeglib.media.extractor.IExtractor
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread


/**
 * 解码器基类
 *
 * @author Liao Jianpeng
 * @Date 2022/2/21
 * @email 329524627@qq.com
 * @Description :
 */
abstract class BaseDecoder(private val filePath: String) {

    private val TAG = "BaseDecoder"

    //-------------线程相关------------------------

    /**
     * 线程等待锁
     */
    private val mLock = Object()


    //---------------状态相关-----------------------
    /**
     * 音视频解码器
     */
    private var mCodec: MediaCodec? = null

    /**
     * 音视频数据读取器
     */
    private var mExtractor: IExtractor? = null

    /**
     * 解码输入缓存区
     */
    private var mInputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码输出缓存区
     */
    private var mOutputBuffers: Array<ByteBuffer>? = null

    /**
     * 解码数据信息
     */
    private var mBufferInfo: MediaCodec.BufferInfo? = null

    @Volatile
    private var decodeState = DecodeState.STOP

    var stateListener: IDecoderStateListener? = null

    /**
     * 流数据是否结束
     */
    private var mIsEOS = false

    /**
     * 时长 ms
     */
    private var mDuration: Long = 0

    /**
     * 开始解码时间，用于音视频同步 ms
     */
    private var mStartTimeForSync = -1L

    private var mProgress = 0L

    // 是否需要音视频渲染同步
    private var mSyncRender = true

    @Volatile
    private var seekToPos = -1

    private fun startDecodeThread() {
        thread {
            if (init()) {
                loopDecode()
            }
            doneDecode()
            release()
        }
    }

    private fun init(): Boolean {
        //【解码步骤：1. 初始化，并启动解码器】
        if (filePath.isEmpty() || !File(filePath).exists()) {
            Log.w(TAG, "文件路径为空")
            stateListener?.decoderError(this, "文件路径为空")
            return false
        }

        if (!check()) return false

        //初始化数据提取器
        mExtractor = initExtractor(filePath)
        val mediaFormat = mExtractor!!.getFormat()
        if (mExtractor == null || mediaFormat == null) {
            Log.w(TAG, "无法解析文件")
            stateListener?.decoderError(this, "无法解析文件")
            return false
        }

        //初始化参数
        try {
            mDuration = mediaFormat.getLong(MediaFormat.KEY_DURATION) / 1000
            initSpecParams(mediaFormat)
        } catch (e: Exception) {
            stateListener?.decoderError(this, "初始化参数失败")
            return false
        }

        //初始化渲染器
        if (!initRender()) {
            stateListener?.decoderError(this, "初始化渲染器失败")
            return false
        }

        //初始化解码器
        try {
            val type = mediaFormat.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(type!!)
            configCodec(mCodec!!, mediaFormat)
            mCodec!!.start()

            mInputBuffers = mCodec?.inputBuffers
            mOutputBuffers = mCodec?.outputBuffers
        } catch (e: Exception) {
            stateListener?.decoderError(this, "初始化解码器失败")
            return false
        }
        mBufferInfo = MediaCodec.BufferInfo()
        decodeState = DecodeState.READY
        stateListener?.decoderReady(this, mDuration / 1000)
        return true
    }

    private fun loopDecode() {
        try {
            Log.i(TAG, "开始解码")
            while (true) {
                if (decodeState == DecodeState.PAUSE) {
                    waitDecode()
                    // ---------【同步时间矫正】-------------
                    //恢复同步的起始时间，即去除等待流失的时间
                    mStartTimeForSync = System.currentTimeMillis() - getCurTimeStamp()
                }

                if (decodeState == DecodeState.STOP || decodeState == DecodeState.FINISH) {
                    break
                }

                if (seekToPos >= 0) {
                    val timeUs = (seekToPos * 1000 * 1000).toLong()
                    mExtractor?.seek(timeUs)
                }

                if (mStartTimeForSync == -1L) {
                    mStartTimeForSync = System.currentTimeMillis()
                }

                //如果数据没有解码完毕，将数据推入解码器解码
                if (!mIsEOS) {
                    //【解码步骤：2. 将数据压入解码器输入缓冲】
                    mIsEOS = pushBufferToDecoder()
                }

                //【解码步骤：3. 将解码好的数据从缓冲区拉取出来】
                val index = pullBufferFromDecoder()
                if (index >= 0) {
                    // ---------【音视频同步】-------------
                    if (mSyncRender) {
                        sleepRender()
                    }
                    //【解码步骤：4. 渲染】
                    if (mSyncRender) {// 如果只是用于编码合成新视频，无需渲染
                        render(mOutputBuffers!![index], mBufferInfo!!)
                    }

                    val current = getCurTimeStamp() / 1000
                    if (mProgress != current) {
                        mProgress = current
                        //将解码数据传递出去
                        val frame = Frame()
                        frame.buffer = mOutputBuffers!![index]
                        frame.setBufferInfo(mBufferInfo!!)
                        stateListener?.decodeOneFrame(this, frame, current)
                    }

                    //【解码步骤：5. 释放输出缓冲】
                    mCodec!!.releaseOutputBuffer(index, true)
                }

                //【解码步骤：6. 判断解码是否完成】
                if (mBufferInfo!!.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "解码结束")
                    decodeState = DecodeState.FINISH
                    stateListener?.decoderFinish(this)
                } else {
                    if (decodeState == DecodeState.READY) {
                        decodeState = DecodeState.DECODING
                        stateListener?.decoderPlay(this)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pushBufferToDecoder(): Boolean {
        val inputBufferIndex = mCodec!!.dequeueInputBuffer(1000)
        var isEndOfStream = false

        if (inputBufferIndex >= 0) {
            val inputBuffer = mInputBuffers!![inputBufferIndex]
            val sampleSize = mExtractor!!.readBuffer(inputBuffer)

            if (sampleSize < 0) {
                //如果数据已经取完，压入数据结束标志：MediaCodec.BUFFER_FLAG_END_OF_STREAM
                mCodec!!.queueInputBuffer(
                    inputBufferIndex, 0, 0,
                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEndOfStream = true
            } else {
                mCodec!!.queueInputBuffer(
                    inputBufferIndex, 0,
                    sampleSize, mExtractor!!.getCurrentTimestamp(), 0
                )
            }
        }
        return isEndOfStream
    }

    private fun pullBufferFromDecoder(): Int {

        // 查询是否有解码完成的数据，index >=0 时，表示数据有效，并且index为缓冲区索引
        when (val index = mCodec!!.dequeueOutputBuffer(mBufferInfo!!, 1000)) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
            MediaCodec.INFO_TRY_AGAIN_LATER -> {}
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                mOutputBuffers = mCodec!!.outputBuffers
            }
            else -> {
                return index
            }
        }
        return -1
    }

    private fun sleepRender() {
        val passTime = System.currentTimeMillis() - mStartTimeForSync
        val curTime = getCurTimeStamp()
        if (curTime > passTime) {
            Thread.sleep(curTime - passTime)
        }
        if (seekToPos >= 0) {
            mStartTimeForSync = System.currentTimeMillis() - mExtractor!!.getCurrentTimestamp()
            seekToPos = -1
        }
    }

    private fun release() {
        try {
            Log.i(TAG, "解码停止，释放解码器")
            mStartTimeForSync = -1
            mIsEOS = false
            mExtractor?.stop()
            mCodec?.stop()
            mCodec?.release()
            stateListener?.decoderStop(this)
        } catch (e: Exception) {
        }
    }

    /**
     * 解码线程进入等待
     */
    private fun waitDecode() {
        try {
            synchronized(mLock) {
                mLock.wait()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续运行
     */
    protected fun notifyDecode() {
        try {
            synchronized(mLock) {
                mLock.notifyAll()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun play() {
        if (decodeState == DecodeState.PAUSE) {
            decodeState = DecodeState.DECODING
            stateListener?.decoderPlay(this)
            notifyDecode()
        } else if (decodeState == DecodeState.FINISH || decodeState == DecodeState.STOP) {
            startDecodeThread()
        }
    }


    fun pause() {
        if (decodeState == DecodeState.READY || decodeState == DecodeState.DECODING) {
            decodeState = DecodeState.PAUSE
            stateListener?.decoderPause(this)
        }
    }

    fun stop() {
        if (decodeState != DecodeState.STOP) {
            decodeState = DecodeState.STOP
            stateListener?.decoderStop(this)
            notifyDecode()
        }
    }

    fun seekTo(pos: Int) {
        seekToPos = pos
        decodeState = DecodeState.DECODING
        stateListener?.decoderPlay(this)
        notifyDecode()
    }


    fun getDuration(): Long {
        return mDuration
    }

    /**
     * 当前播放时间 ms
     * @return Long
     */
    private fun getCurTimeStamp(): Long {
        return (mBufferInfo?.presentationTimeUs ?: 0) / 1000
    }


    fun getMediaFormat(): MediaFormat? {
        return mExtractor?.getFormat()
    }


    /**
     * 检查子类参数
     */
    abstract fun check(): Boolean

    /**
     * 初始化数据提取器
     */
    abstract fun initExtractor(path: String): IExtractor

    /**
     * 初始化子类自己特有的参数
     */
    abstract fun initSpecParams(format: MediaFormat)

    /**
     * 配置解码器
     */
    abstract fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean

    /**
     * 初始化渲染器
     */
    abstract fun initRender(): Boolean

    /**
     * 渲染
     */
    abstract fun render(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    )

    /**
     * 结束解码
     */
    abstract fun doneDecode()
}