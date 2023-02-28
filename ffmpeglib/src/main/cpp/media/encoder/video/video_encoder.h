//
// Created by Liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_VIDEO_ENCODER_H
#define FFMPEGPLAY_VIDEO_ENCODER_H


#include "../base_encoder.h"

class VideoEncoder : public BaseEncoder {
private:
    const char *TAG = "VideoEncoder";
    // 视频编码帧率
    static const int ENCODE_VIDEO_FPS = 25;

    SwsContext *m_sws_ctx = NULL;

    AVFrame *m_yuv_frame = NULL;

    int m_width = 0, m_height = 0;

    void InitYUVFrame();

protected:

    void InitContext(AVCodecContext *codec_ctx) override;

    int ConfigureMuxerStream(Mp4Muxer *muxer, AVCodecContext *ctx) override;

    AVFrame *DealFrame(OneFrame *one_frame) override;

    void Release() override;

public:
    VideoEncoder(Mp4Muxer *muxer, int width, int height);

};


#endif //FFMPEGPLAY_VIDEO_ENCODER_H
