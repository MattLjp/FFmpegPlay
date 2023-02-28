//
// Created by Liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_AUDIO_RENDER_H
#define FFMPEGPLAY_AUDIO_RENDER_H


class AudioRender {
public:
    virtual ~AudioRender() {}

    virtual void InitRender() = 0;

    virtual void RenderAudioFrame(uint8_t *pcm, int size) = 0;

    virtual void ClearAudioCache() = 0;

    virtual void UnInitRender() = 0;
};

#endif //FFMPEGPLAY_AUDIO_RENDER_H
