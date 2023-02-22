//
// Created by liaojp on 2023/1/11.
//

#ifndef FFMPEGPLAY_SYNTHESIZER_H
#define FFMPEGPLAY_SYNTHESIZER_H
//
//
//#include "../muxer/muxer.h"
//#include "../decoder/video/video_decoder.h"
//#include "../decoder/audio/audio_decoder.h"
//#include "../render/video/opengl_render.h"
//#include "../muxer/mp4_muxer.h"
//#include "../encoder/audio/a_encoder.h"
//#include "../encoder/video/v_encoder.h"
//#include "../decoder/i_decode_state_cb.h"
//
//class Synthesizer: Muxer, IDecodeStateCb, IEncodeStateCb, OpenGLPixelReceiver {
//private:
//
//    VideoDecoder *m_video_decoder = NULL;
//
//    AudioDecoder *m_audio_decoder = NULL;
//
//    OpenGLRender *m_gl_render = NULL;
//
//    DrawerProxy *m_drawer_proxy = NULL;
//
//    Mp4Muxer *m_mp4_muxer = NULL;
//
//    VideoEncoder *m_v_encoder = NULL;
//
//    AudioEncoder *m_a_encoder = NULL;
//
//    OneFrame *m_cur_v_frame = NULL;
//    OneFrame *m_cur_a_frame = NULL;
//
//public:
//    Synthesizer(JNIEnv *env, jstring src_path, jstring dst_path);
//    ~Synthesizer();
//
//    void Start();
//
//    void ReceivePixel(uint8_t *data) override ;
//    void OnMuxFinished() override;
//
//    void DecodePrepare(IDecoder *decoder) override;
//    void DecodeReady(IDecoder *decoder) override;
//    void DecodeRunning(IDecoder *decoder) override;
//    void DecodePause(IDecoder *decoder) override;
//    bool DecodeOneFrame(IDecoder *decoder, OneFrame *frame) override;
//    void DecodeFinish(IDecoder *decoder) override;
//    void DecodeStop(IDecoder *decoder) override;
//
//    void EncodeStart() override;
//
//    void EncodeSend() override;
//
//    void EncodeFrame(void *data) override;
//    void EncodeProgress(long time) override;
//    void EncodeFinish() override;
//};


#endif //FFMPEGPLAY_SYNTHESIZER_H
