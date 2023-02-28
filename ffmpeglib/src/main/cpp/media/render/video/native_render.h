//
// Created by Liaojp on 2023/1/7.
//

#ifndef FFMPEGPLAY_NATIVE_RENDER_H
#define FFMPEGPLAY_NATIVE_RENDER_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <jni.h>
#include "video_render.h"
#include "../../../utils/LogUtil.h"

extern "C" {
#include <libavutil/mem.h>
};

class NativeRender : public VideoRender {
public:
    NativeRender(JNIEnv *env, jobject surface);

    ~NativeRender();

    void InitRender(int video_width, int video_height, int *dst_size) override;

    void RenderVideoFrame(OneFrame *one_frame) override;

    void UnInitRender() override;

private:
    const char *TAG = "NativeRender";

    // 存放输出到屏幕的缓存数据
    ANativeWindow_Buffer m_out_buffer;

    // 本地窗口
    ANativeWindow *m_native_window = nullptr;

    //显示的目标宽
    int m_dst_w;

    //显示的目标高
    int m_dst_h;
};


#endif //FFMPEGPLAY_NATIVE_RENDER_H
