//
// Created by liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_ENCODER_H
#define FFMPEGPLAY_ENCODER_H

#include "../one_frame.h"
#include "encoder_state.h"

class Encoder {
public:
    virtual void PushFrame(OneFrame *one_frame) = 0;

    virtual bool TooMuchData() = 0;

    virtual void SetStateReceiver(EncoderState *cb) = 0;
};

#endif //FFMPEGPLAY_ENCODER_H
