//
// Created by Liaojp on 2023/1/7.
//

#include "video_decoder.h"
#include "../../one_frame.h"

void VideoDecoder::OnDecoderReady() {
    if (m_video_render) {
        //设置视频显示宽高
        int dst_size[2] = {-1, -1};
        m_video_render->InitRender(videoWidth(), videoHeight(), dst_size);

        m_dst_w = dst_size[0];
        m_dst_h = dst_size[1];
        if (m_dst_w == -1) {
            m_dst_w = videoWidth();
        }
        if (m_dst_h == -1) {
            m_dst_w = videoHeight();
        }
        LOGI("VideoDecoder::dst %d, %d", m_dst_w, m_dst_h)

        //初始化缓存
        m_rgb_frame = av_frame_alloc();
        // 获取缓存大小
        int numBytes = av_image_get_buffer_size(DST_FORMAT, m_dst_w, m_dst_h, 1);
        // 分配内存
        m_buf_for_rgb_frame = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
        // 将内存分配给RgbFrame，并将内存格式化为三个通道后，分别保存其地址
        av_image_fill_arrays(m_rgb_frame->data, m_rgb_frame->linesize,
                             m_buf_for_rgb_frame, DST_FORMAT, m_dst_w, m_dst_h, 1);

        // 初始化格式转换工具
        m_sws_ctx = sws_getContext(videoWidth(), videoHeight(), GetAVPixelFormat(),
                                   m_dst_w, m_dst_h, DST_FORMAT,
                                   SWS_FAST_BILINEAR, NULL, NULL, NULL);

    } else {
        LOGE("VideoDecoder::OnDecoderReady m_video_render == null");
    }
}

void VideoDecoder::OnDecoderDone() {
    if (m_rgb_frame) {
        av_frame_free(&m_rgb_frame);
        m_rgb_frame = nullptr;
    }
    if (m_buf_for_rgb_frame) {
        free(m_buf_for_rgb_frame);
        m_buf_for_rgb_frame = nullptr;
    }
    if (m_sws_ctx) {
        sws_freeContext(m_sws_ctx);
        m_sws_ctx = nullptr;
    }
    if (m_video_render) {
        m_video_render->UnInitRender();
        delete m_video_render;
        m_video_render = nullptr;
    }
}

void VideoDecoder::OnFrameAvailable(AVFrame *frame) {
    if (m_video_render) {
        sws_scale(m_sws_ctx, frame->data, frame->linesize, 0,
                  videoHeight(), m_rgb_frame->data, m_rgb_frame->linesize);
        OneFrame *one_frame = new OneFrame(m_rgb_frame->data[0], m_rgb_frame->linesize[0], frame->pts, GetTimeBase(),
                                           NULL, false);
        m_video_render->RenderVideoFrame(one_frame);
    }

}
