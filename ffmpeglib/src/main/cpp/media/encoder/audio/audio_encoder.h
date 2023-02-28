//
// Created by Liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_AUDIO_ENCODER_H
#define FFMPEGPLAY_AUDIO_ENCODER_H


#include "../base_encoder.h"

extern "C" {
#include <libswresample/swresample.h>
#include <libavutil/samplefmt.h>
};


class AudioEncoder : public BaseEncoder {

private:
    // 音频编码格式：浮点型数据（32位）
    static const AVSampleFormat ENCODE_AUDIO_DEST_FORMAT = AV_SAMPLE_FMT_FLTP;
    // 音频编码采样率
    static const int ENCODE_AUDIO_DEST_SAMPLE_RATE = 44100;
    // 音频编码通道数
    static const int ENCODE_AUDIO_DEST_CHANNEL_COUNTS = 2;
    // 音频编码声道格式
    static const uint64_t ENCODE_AUDIO_DEST_CHANNEL_LAYOUT = AV_CH_LAYOUT_STEREO;
    // 音频编码比特率
    static const int ENCODE_AUDIO_DEST_BIT_RATE = 64000;
    // ACC音频一帧采样数
    static const int ACC_NB_SAMPLES = 1024;

    AVFrame *m_frame = NULL;

    void InitFrame();

protected:
    void InitContext(AVCodecContext *codec_ctx) override;

    int ConfigureMuxerStream(Mp4Muxer *muxer, AVCodecContext *ctx) override;

    AVFrame *DealFrame(OneFrame *one_frame) override;

    void Release() override;

public:
    AudioEncoder(Mp4Muxer *muxer);
};


#endif //FFMPEGPLAY_AUDIO_ENCODER_H
