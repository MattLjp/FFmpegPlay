//
// Mp4混合器
// Created by liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_MP4_MUXER_H
#define FFMPEGPLAY_MP4_MUXER_H

#include "muxer.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
};

typedef void (*mux_finish_cb)();

#define MAX_PATH   2048

class Mp4Muxer {
private:

    const char *TAG = "Mp4Muxer";

    //文件地址
    char m_url[MAX_PATH] = {0};

    AVFormatContext *m_fmt_ctx = NULL;

    bool m_audio_configured = false;

    bool m_audio_end = false;

    bool m_video_configured = false;

    bool m_video_end = false;

    Muxer *m_mux_finish_cb = NULL;

    int AddStream(AVCodecContext *ctx);

public:
    Mp4Muxer();

    ~Mp4Muxer();

    void SetMuxFinishCallback(Muxer *cb) {
        this->m_mux_finish_cb = cb;
    }

    AVRational GetTimeBase(int stream_index) {
        return m_fmt_ctx->streams[stream_index]->time_base;
    }

    void Init(const char *url);

    int AddVideoStream(AVCodecContext *ctx);

    int AddAudioStream(AVCodecContext *ctx);

    void Start();

    void Write(AVPacket *pkt);

    void EndVideoStream();

    void EndAudioStream();

    void Release();
};

#endif //FFMPEGPLAY_MP4_MUXER_H
