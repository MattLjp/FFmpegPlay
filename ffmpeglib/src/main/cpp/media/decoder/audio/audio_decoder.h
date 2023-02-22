//
// Created by liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_AUDIO_DECODER_H
#define FFMPEGPLAY_AUDIO_DECODER_H

extern "C" {
#include <libavutil/samplefmt.h>
#include <libswresample/swresample.h>
#include <libavutil/opt.h>
#include <libavutil/audio_fifo.h>
};

#include "../base_decoder.h"
#include "../../render/audio/audio_render.h"


class AudioDecoder : public BaseDecoder {
public:
    AudioDecoder(char *url) {
        Init(url, AVMEDIA_TYPE_AUDIO);
    }

    ~AudioDecoder() {
        UnInit();
    }

    void SetAudioRender(AudioRender *audioRender) {
        m_audio_render = audioRender;
    }

protected:
    void OnDecoderReady() override;

    void OnDecoderDone() override;

    void OnFrameAvailable(AVFrame *frame) override;

    void ClearCache() override;

private:
    const char *TAG = "AudioDecoder";
    // 音频编码采样率
    static const int AUDIO_DST_SAMPLE_RATE = 44100;
    // 音频编码通道数
    static const int AUDIO_DST_CHANNEL_COUNTS = 2;
    // 音频编码声道格式
    static const uint64_t AUDIO_DST_CHANNEL_LAYOUT = AV_CH_LAYOUT_STEREO;
    // 音频编码比特率
    static const int AUDIO_DST_BIT_RATE = 64000;
    // ACC音频一帧采样数
    static const int ACC_NB_SAMPLES = 1024;
    //采样格式：16位
    static const AVSampleFormat DST_SAMPLE_FORMAT = AV_SAMPLE_FMT_S16;

    //音频播放器
    AudioRender *m_audio_render = nullptr;

    //音频重采样上下文
    SwrContext *m_swr_ctx = nullptr;

    // 输出缓冲
    uint8_t *m_audio_out_buffer = nullptr;

    // 重采样后，每个通道包含的采样数
    // acc默认为1024，重采样后可能会变化
    int m_dest_sample = 0;

    // 重采样以后，一帧数据的大小
    int m_dest_data_size = 0;
};


#endif //FFMPEGPLAY_AUDIO_DECODER_H
