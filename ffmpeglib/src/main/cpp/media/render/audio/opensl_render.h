//
// Created by Liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_OPENSL_RENDER_H
#define FFMPEGPLAY_OPENSL_RENDER_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <queue>
#include <string>
#include <thread>
#include "../../../utils/LogUtil.h"
#include "../../pcm_data.h"
#include "audio_render.h"

extern "C" {
#include <libavutil/mem.h>
};
using namespace std;

#define MAX_QUEUE_BUFFER_SIZE 3

class OpenSLRender : public AudioRender {
public:
    OpenSLRender();

    ~OpenSLRender();

    void InitRender() override;

    void RenderAudioFrame(uint8_t *pcm, int size) override;

    void ClearAudioCache() override;

    void UnInitRender() override;

private:
    const char *TAG = "OpenSLRender";

    // 引擎接口
    SLObjectItf m_engine_obj = nullptr;
    SLEngineItf m_engine = nullptr;

    //混音器
    SLObjectItf m_output_mix_obj = nullptr;
    SLEnvironmentalReverbItf m_output_mix_evn_reverb = nullptr;
    SLEnvironmentalReverbSettings m_reverb_settings = SL_I3DL2_ENVIRONMENT_PRESET_DEFAULT;

    //pcm播放器
    SLObjectItf m_pcm_player_obj = nullptr;
    SLPlayItf m_pcm_player = nullptr;
    SLVolumeItf m_pcm_player_volume = nullptr;
    const SLuint32 SL_QUEUE_BUFFER_COUNT = 2;

    //缓冲器队列接口
    SLAndroidSimpleBufferQueueItf m_pcm_buffer;

    queue<PcmData *> m_data_queue;

    volatile bool m_exit = false;
    mutex m_mutex;
    condition_variable m_cond;

    static void sRenderPcm(OpenSLRender *that);

    static void sReadPcmBufferCbFun(SLAndroidSimpleBufferQueueItf bufferQueueItf, void *context);

    // 创建引擎
    int CreateEngine();

    // 创建混音器
    int CreateOutputMixer();

    // 创建播放器
    int CreatePlayer();

    // 开始播放渲染
    void StartRender();

    // 音频数据压入缓冲队列
    void BlockEnqueue();

    // 检查是否发生错误
    int CheckError(SLresult result, std::string hint);

    int GetAudioFrameQueueSize();

    void WaitForCache() {
        unique_lock<mutex> lock(m_mutex);
        m_cond.wait(lock);
    }

    void NotifyAllCache() {
        unique_lock<mutex> lock(m_mutex);
        m_cond.notify_all();
    }
};


#endif //FFMPEGPLAY_OPENSL_RENDER_H
