//
// Created by Liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_OPENGL_PIXEL_OUTPUT_H
#define FFMPEGPLAY_OPENGL_PIXEL_OUTPUT_H

#include <stdint.h>

class OpenGLPixelReceiver {
public:
    virtual void ReceivePixel(uint8_t *rgba) = 0;
};

#endif //FFMPEGPLAY_OPENGL_PIXEL_OUTPUT_H
