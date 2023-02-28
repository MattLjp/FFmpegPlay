//
// Created by Liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_ENCODER_STATE_H
#define FFMPEGPLAY_ENCODER_STATE_H

class EncoderState {
public:
    virtual void EncodeStart() = 0;

    virtual void EncodeSend() = 0;

    virtual void EncodeFrame(void *data) = 0;

    virtual void EncodeProgress(long time) = 0;

    virtual void EncodeFinish() = 0;
};


#endif //FFMPEGPLAY_ENCODER_STATE_H
