//
// 重打包
// Created by Liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_FF_REPACK_H
#define FFMPEGPLAY_FF_REPACK_H

extern "C" {
#include <libavformat/avformat.h>
};

class FFRepack {
private:
    const char *TAG = "FFRepack";

    AVFormatContext *m_in_format_cxt = NULL;

    AVFormatContext *m_out_format_cxt = NULL;

    int OpenSrcFile(const char *srcPath);

    int InitMuxerParams(const char *destPath);

public:
    void Init(const char *in_path, const char *out_path);

    void Start();

    void Write(AVPacket pkt);

    void Release();
};


#endif //FFMPEGPLAY_FF_REPACK_H
