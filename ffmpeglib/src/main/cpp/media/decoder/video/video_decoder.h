//
// Created by liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_VIDEO_DECODER_H
#define FFMPEGPLAY_VIDEO_DECODER_H

extern "C" {
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
#include <libavcodec/jni.h>
};

#include "../base_decoder.h"
#include "../../render/video/video_render.h"

class VideoDecoder : public BaseDecoder {
public:
    VideoDecoder(char *url) {
        Init(url, AVMEDIA_TYPE_VIDEO);
    }

    ~VideoDecoder() {
        UnInit();
    }

    void SetVideoRender(VideoRender *videoRender) {
        m_video_render = videoRender;
    }

protected:
    void OnDecoderReady() override;

    void OnDecoderDone() override;

    void OnFrameAvailable(AVFrame *frame) override;

private:
    const char *TAG = "VideoDecoder";

    //视频数据目标格式
    const AVPixelFormat DST_FORMAT = AV_PIX_FMT_RGBA;

    //存放YUV转换为RGB后的数据
    AVFrame *m_rgb_frame = nullptr;

    uint8_t *m_buf_for_rgb_frame = nullptr;

    //视频格式转换器
    SwsContext *m_sws_ctx = nullptr;

    //视频渲染器
    VideoRender *m_video_render = nullptr;

    //显示的目标宽
    int m_dst_w;
    //显示的目标高
    int m_dst_h;
};


#endif //FFMPEGPLAY_VIDEO_DECODER_H
