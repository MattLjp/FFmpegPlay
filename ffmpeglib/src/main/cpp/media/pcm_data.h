//
// Created by Liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_PCM_DATA_H
#define FFMPEGPLAY_PCM_DATA_H

class PcmData {
public:
    PcmData(uint8_t *pcm, int size) {
        this->pcm = pcm;
        this->size = size;
    }

    ~PcmData() {
        if (pcm != NULL) {
            //释放已使用的内存
            free(pcm);
            pcm = NULL;
            used = false;
        }
    }

    uint8_t *pcm = NULL;
    int size = 0;
    bool used = false;
};

#endif //FFMPEGPLAY_PCM_DATA_H
