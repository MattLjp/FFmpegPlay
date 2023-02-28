//
// Created by Liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_BASE_ENCODER_H
#define FFMPEGPLAY_BASE_ENCODER_H

#include <thread>
#include <mutex>
#include <queue>
#include "encoder.h"
#include "../muxer/mp4_muxer.h"
#include "../../utils/LogUtil.h"

extern "C" {
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavformat/avformat.h"
#include "libavutil/opt.h"
#include "libavutil/frame.h"
};

class BaseEncoder : public Encoder {
private:
    const char *TAG = "BaseEncoder";

    // 编码格式 ID
    AVCodecID m_codec_id;

    // 线程等待锁变量
    std::mutex m_mutex;
    std::condition_variable m_cond;

    // 编码器
    const AVCodec *m_codec = NULL;

    // 编码上下文
    AVCodecContext *m_codec_ctx = NULL;

    // 编码数据包
    AVPacket *m_encoded_pkt = NULL;

    // 写入Mp4的输入流索引
    int m_encode_stream_index = 0;

    // 原数据时间基
    AVRational m_src_time_base;

    // 缓冲队列
    std::queue<OneFrame *> m_src_frames;

    // 操作数据锁
    std::mutex m_frames_lock;

    // 状态回调
    EncoderState *m_state_cb = NULL;

    bool InitFFEncoder();

    /**
     * 循环拉去已经编码的数据，直到没有数据或者编码完毕
     * @return true 编码结束；false 编码未完成
     */
    bool DrainEncode();

    /**
     * 编码一帧数据
     * @return 错误信息
     */
    int EncodeOneFrame();

    /**
     * 新建编码线程
     */
    void CreateEncodeThread();

    /**
     * 解码静态方法，给线程调用
     */
    static void Encode(std::shared_ptr<BaseEncoder> that);

    void OpenEncoder();

    /**
     * 循环编码
     */
    void LoopEncode();

    void DoRelease();

    void Wait() {
        std::unique_lock<std::mutex> lock(m_mutex);
        m_cond.wait(lock);
    }

    void SendSignal() {
        std::unique_lock<std::mutex> lock(m_mutex);
        m_cond.notify_all();
    }

protected:
    Mp4Muxer *m_muxer = NULL;

    virtual void InitContext(AVCodecContext *codec_ctx) = 0;

    virtual int ConfigureMuxerStream(Mp4Muxer *muxer, AVCodecContext *ctx) = 0;

    virtual AVFrame *DealFrame(OneFrame *one_frame) = 0;

    virtual void Release() = 0;

public:
    virtual~ BaseEncoder() {};

    void Init(Mp4Muxer *muxer, AVCodecID codec_id);

    void PushFrame(OneFrame *one_frame) override;

    bool TooMuchData() override {
        return m_src_frames.size() > 100;
    }

    void SetStateReceiver(EncoderState *cb) override {
        this->m_state_cb = cb;
    }
};

#endif //FFMPEGPLAY_BASE_ENCODER_H