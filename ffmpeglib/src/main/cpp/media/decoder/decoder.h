//
// Created by Liaojp on 2023/1/6.
//

#ifndef FFMPEGPLAY_DECODER_H
#define FFMPEGPLAY_DECODER_H

typedef void (*MessageCallback)(void *, int, int);

enum DecoderMsg {
    MSG_DECODER_INIT_ERROR,
    MSG_DECODER_PREPARE,
    MSG_DECODER_PLAY,
    MSG_DECODER_PAUSE,
    MSG_DECODER_FINISH,
    MSG_DECODER_STOP,
    MSG_DECODER_TIME
};

class Decoder {
public:
    virtual void Start() = 0;

    virtual void Pause() = 0;

    virtual void Stop() = 0;

    virtual int GetDuration() = 0;

    virtual void SeekToPosition(int position) = 0;

    virtual int GetCurrentPosition() = 0;

    virtual void SetMessageCallback(void *context, MessageCallback callback) = 0;
};

#endif //FFMPEGPLAY_DECODER_H
