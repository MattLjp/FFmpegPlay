//
// Created by Liaojp on 2023/1/7.
//

#include "audio_decoder.h"

void AudioDecoder::OnDecoderReady() {
    if (m_audio_render) {
        AVCodecContext *codec_ctx = GetCodecContext();

        //初始化格式转换工具
        m_swr_ctx = swr_alloc();
        // 配置输入/输出通道类型
        av_opt_set_int(m_swr_ctx, "in_channel_layout", codec_ctx->channel_layout, 0);
        // 这里 AUDIO_DEST_CHANNEL_LAYOUT = AV_CH_LAYOUT_STEREO，即 立体声
        av_opt_set_int(m_swr_ctx, "out_channel_layout", AUDIO_DST_CHANNEL_LAYOUT, 0);

        // 配置输入/输出采样率
        av_opt_set_int(m_swr_ctx, "in_sample_rate", codec_ctx->sample_rate, 0);
        av_opt_set_int(m_swr_ctx, "out_sample_rate", AUDIO_DST_SAMPLE_RATE, 0);

        // 配置输入/输出数据格式
        av_opt_set_sample_fmt(m_swr_ctx, "in_sample_fmt", codec_ctx->sample_fmt, 0);
        av_opt_set_sample_fmt(m_swr_ctx, "out_sample_fmt", DST_SAMPLE_FORMAT, 0);

        swr_init(m_swr_ctx);

        // 重采样后一个通道采样数
        m_dest_sample = (int) av_rescale_rnd(ACC_NB_SAMPLES, AUDIO_DST_SAMPLE_RATE, codec_ctx->sample_rate,
                                             AV_ROUND_UP);
        // 重采样后一帧数据的大小
        m_dest_data_size = av_samples_get_buffer_size(NULL, AUDIO_DST_CHANNEL_COUNTS, m_dest_sample, DST_SAMPLE_FORMAT,
                                                      1);

        m_audio_out_buffer = (uint8_t *) malloc(m_dest_data_size);

        //初始化音频渲染器
        m_audio_render->InitRender();
    } else {
        LOGE("AudioDecoder::OnDecoderReady m_audio_render == null");
    }
}

void AudioDecoder::OnFrameAvailable(AVFrame *frame) {
    if (m_audio_render) {
        // 转换，返回每个通道的样本数
        int result = swr_convert(m_swr_ctx, &m_audio_out_buffer, m_dest_data_size / 2, (const uint8_t **) frame->data,
                                 frame->nb_samples);
        if (result > 0) {
            m_audio_render->RenderAudioFrame(m_audio_out_buffer, m_dest_data_size);
        }
    }
}

void AudioDecoder::OnDecoderDone() {
    if (m_audio_out_buffer) {
        free(m_audio_out_buffer);
        m_audio_out_buffer = nullptr;
    }

    if (m_swr_ctx) {
        swr_free(&m_swr_ctx);
        m_swr_ctx = nullptr;
    }
    if (m_audio_render) {
        m_audio_render->UnInitRender();
        delete m_audio_render;
        m_audio_render = nullptr;
    }
}

void AudioDecoder::ClearCache() {
    if(m_audio_render) {
        m_audio_render->ClearAudioCache();
    }
}
