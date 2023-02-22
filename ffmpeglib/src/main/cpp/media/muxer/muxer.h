//
// Created by liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_MUXER_H
#define FFMPEGPLAY_MUXER_H

class Muxer {
public:
    virtual void OnMuxFinished() = 0;
};

#endif //FFMPEGPLAY_MUXER_H
