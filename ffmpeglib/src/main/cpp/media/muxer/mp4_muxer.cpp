//
// Created by liaojp on 2023/1/11.
//

#include "mp4_muxer.h"

#include "../../utils/LogUtil.h"

Mp4Muxer::Mp4Muxer() {

}

Mp4Muxer::~Mp4Muxer() {
}

void Mp4Muxer::Init(const char *url) {
    strcpy(m_url, url);
    //新建输出上下文
    avformat_alloc_output_context2(&m_fmt_ctx, NULL, NULL, m_url);

}

int Mp4Muxer::AddVideoStream(AVCodecContext *ctx) {
    int stream_index = AddStream(ctx);
    m_video_configured = true;
    Start();
    return stream_index;
}

int Mp4Muxer::AddAudioStream(AVCodecContext *ctx) {
    int stream_index = AddStream(ctx);
    m_audio_configured = true;
    Start();
    return stream_index;
}

int Mp4Muxer::AddStream(AVCodecContext *ctx) {
    AVStream *video_stream = avformat_new_stream(m_fmt_ctx, NULL);
    avcodec_parameters_from_context(video_stream->codecpar, ctx);
    video_stream->codecpar->codec_tag = 0;
    return video_stream->index;
}

void Mp4Muxer::Start() {
    if (m_video_configured && m_audio_configured) {
        av_dump_format(m_fmt_ctx, 0, m_url, 1);
        //打开文件输入
        int ret = avio_open(&m_fmt_ctx->pb, m_url, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGE("Mp4Muxer::Open av io fail")
            return;
        } else {
            LOGI("Mp4Muxer::Open av io: %s", m_url)
        }
        //写入头部信息
        ret = avformat_write_header(m_fmt_ctx, NULL);
        if (ret < 0) {
            LOGE("Mp4Muxer::Write header fail")
            return;
        } else {
            LOGI("Mp4Muxer::Write header success")
        }
    }
}

void Mp4Muxer::Write(AVPacket *pkt) {
//    uint64_t time = uint64_t (pkt->pts*av_q2d(GetTimeBase(pkt->stream_index))*1000);
    int ret = av_interleaved_write_frame(m_fmt_ctx, pkt);
//    LOGE("Mp4Muxer::Write one frame pts: %lld, ret = %s", time , av_err2str(ret))
}

void Mp4Muxer::EndAudioStream() {
    LOGI("Mp4Muxer::End audio stream")
    m_audio_end = true;
    Release();
}

void Mp4Muxer::EndVideoStream() {
    LOGI("Mp4Muxer::End video stream")
    m_video_end = true;
    Release();
}

void Mp4Muxer::Release() {
    if (m_video_end && m_audio_end) {
        if (m_fmt_ctx) {
            //写入文件尾部
            av_write_trailer(m_fmt_ctx);

            //关闭输出IO
            avio_close(m_fmt_ctx->pb);

            //释放资源
            avformat_free_context(m_fmt_ctx);

            m_fmt_ctx = NULL;
        }
        LOGI("Mp4Muxer::Muxer Release")
        if (m_mux_finish_cb) {
            m_mux_finish_cb->OnMuxFinished();
        }
    }
}