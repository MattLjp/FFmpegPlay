//
// Created by Liaojp on 2023/1/6.
//

#ifndef FFMPEGPLAY_BASE_DECODER_H
#define FFMPEGPLAY_BASE_DECODER_H

#include <jni.h>
#include <thread>
#include "decoder.h"
#include "decode_state.h"
#include "../../utils/LogUtil.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/frame.h>
#include <libavutil/time.h>
#include <libavcodec/jni.h>
};

#define MAX_PATH   2048
using namespace std;

class BaseDecoder : public Decoder {
public:
    virtual~ BaseDecoder() {};

    /**
     * 开始播放
     */
    void Start() override;

    /**
     * 暂停播放
     */
    void Pause() override;

    /**
     * 停止
     */
    void Stop() override;


    /**
     * 时长
     * @return
     */
    int GetDuration() override {
        //ms to s
        return m_duration / 1000;
    }

    /**
     * seek 到某个时间点播放 s
     * @param position
     */
    void SeekToPosition(int position) override;


    /**
     * 当前播放的位置
     * @return
     */
    int GetCurrentPosition() override {
        //单位 s
        return m_current_t / 1000;
    }

    void SetMessageCallback(void *context, MessageCallback callback) override {
        m_msg_ctx = context;
        m_msg_callback = callback;
    }

    /**
     * 视频宽度
     * @return
     */
    int videoWidth() {
        return m_codec_ctx->width;
    }

    /**
     * 视频高度
     * @return
     */
    int videoHeight() {
        return m_codec_ctx->height;
    }

    virtual void ClearCache() {};

protected:
    void *m_msg_ctx = nullptr;
    MessageCallback m_msg_callback = nullptr;


    virtual void Init(const char *url, AVMediaType mediaType);

    virtual void UnInit();

    virtual void OnDecoderReady() = 0;


    virtual void OnDecoderDone() = 0;

    /**
     * 解码数据的回调
     * @param frame
     */
    virtual void OnFrameAvailable(AVFrame *frame) = 0;

    /**
     * 解码器上下文
     * @return
     */
    AVCodecContext *GetCodecContext() {
        return m_codec_ctx;
    }

    /**
     * 视频数据编码格式
     * @return
     */
    AVPixelFormat GetAVPixelFormat() {
        return m_codec_ctx->pix_fmt;
    }

    /**
     * 获取解码时间基
     */
    AVRational GetTimeBase() {
        return m_format_ctx->streams[m_stream_index]->time_base;
    }


private:


    //-------------定义解码相关------------------------------

    // 封装格式上下文
    AVFormatContext *m_format_ctx = nullptr;

    //解码器上下文
    AVCodecContext *m_codec_ctx = nullptr;

    //解码器
    const AVCodec *m_codec = nullptr;

    //编码的数据包
    AVPacket *m_packet = nullptr;

    //解码的帧
    AVFrame *m_frame = nullptr;

    //文件地址
    char m_url[MAX_PATH] = {0};

    //数据流的类型
    AVMediaType m_media_type = AVMEDIA_TYPE_UNKNOWN;

    //当前播放时间 ms
    int64_t m_current_t = 0;

    //播放的起始时间 ms
    int64_t m_started_t = -1;

    //总时长 ms
    int64_t m_duration = 0;

    //播放进度 s
    int m_progress_t = 0;

    //数据流索引
    int m_stream_index = -1;

    // 解码状态
    volatile DecodeState m_state = STOP;

    //seek position s
    volatile int64_t m_seek_position = -1;

    volatile bool m_seek_success = false;

    // -------------------定义线程锁相关-----------------------------
    mutex m_mutex;
    condition_variable m_cond;

    //-----------------私有方法------------------------------------
    int InitFFDecoder();

    void UnInitFFDecoder();

    /**
     * 启动解码线程
     */
    void StartDecodingThread();

    /**
     * 线程函数
     * @param decoder
     */
    static void DoAVDecoding(std::shared_ptr<BaseDecoder> that);

    /**
     * 循环解码
     */
    void LoopDecode();

    /**
     * 获取当前帧时间戳
     */
    void UpdateTimeStamp();

    /**
     * 解码一个packet编码数据
     * @return
     */
    int DecodeOnePacket();

    /**
     * 音视频同步
     */
    void AVSync();

};

#endif //FFMPEGPLAY_BASE_DECODER_H