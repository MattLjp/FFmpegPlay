//
// Created by liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_VIDEO_RENDER_H
#define FFMPEGPLAY_VIDEO_RENDER_H

#include "../../one_frame.h"

class VideoRender {
public:
    virtual ~VideoRender(){}

    virtual void InitRender(int video_width, int video_height, int *dst_size) = 0;

    virtual void RenderVideoFrame(OneFrame *one_frame) = 0;

    virtual void UnInitRender() = 0;
};

#endif //FFMPEGPLAY_VIDEO_RENDER_H
