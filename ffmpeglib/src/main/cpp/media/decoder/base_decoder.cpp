//
// Created by liaojp on 2023/1/6.
//

#include "base_decoder.h"
#include "../../utils/timer.c"

void BaseDecoder::Init(const char *url, AVMediaType mediaType) {
    LOGD("BaseDecoder::Decoder InitFFEncoder, MediaType=%d", mediaType)
    strcpy(m_url, url);
    m_media_type = mediaType;
    StartDecodingThread();
}

void BaseDecoder::UnInit() {
    LOGD("BaseDecoder::Decoder UnInit, MediaType=%d", m_media_type)
}

void BaseDecoder::Start() {
    if (m_state == PAUSE) {
        unique_lock<mutex> lock(m_mutex);
        m_state = DECODING;
        if (m_msg_ctx && m_msg_callback) {
            m_msg_callback(m_msg_ctx, MSG_DECODER_PLAY, 0);
        }
        m_cond.notify_all();
    } else if (m_state == FINISH) {
        SeekToPosition(0);
    }
}

void BaseDecoder::Pause() {
    if (m_state == READY || m_state == DECODING) {
        unique_lock<mutex> lock(m_mutex);
        m_state = PAUSE;
        if (m_msg_ctx && m_msg_callback) {
            m_msg_callback(m_msg_ctx, MSG_DECODER_PAUSE, 0);
        }
    }
}

void BaseDecoder::Stop() {
    if (m_state != STOP) {
        unique_lock<mutex> lock(m_mutex);
        m_state = STOP;
        if (m_msg_ctx && m_msg_callback) {
            m_msg_callback(m_msg_ctx, MSG_DECODER_STOP, 0);
        }
        m_cond.notify_all();
    }
}

void BaseDecoder::SeekToPosition(int position) {
    unique_lock<mutex> lock(m_mutex);
    m_seek_position = position;
    m_state = DECODING;
    if (m_msg_ctx && m_msg_callback) {
        m_msg_callback(m_msg_ctx, MSG_DECODER_PLAY, 0);
    }
    m_cond.notify_all();
}


int BaseDecoder::InitFFDecoder() {
    int result = 0;
    do {
        //1.创建封装格式上下文
        m_format_ctx = avformat_alloc_context();

        //2.打开文件
        if (avformat_open_input(&m_format_ctx, m_url, NULL, NULL) != 0) {
            LOGD("BaseDecoder::InitFFDecoder avformat_open_input fail.");
            result = -1;
            break;
        }
        //3.获取音视频流信息
        if (avformat_find_stream_info(m_format_ctx, NULL) < 0) {
            LOGD("BaseDecoder::InitFFDecoder avformat_find_stream_info fail.");
            result = -1;
            break;
        }

        //4.获取音视频流索引
        for (int i = 0; i < m_format_ctx->nb_streams; i++) {
            if (m_format_ctx->streams[i]->codecpar->codec_type == m_media_type) {
                m_stream_index = i;
                break;
            }
        }
        if (m_stream_index == -1) {
            LOGD("BaseDecoder::InitFFDecoder Fail to find stream index.");
            result = -1;
            break;
        }

        //5.获取解码器参数
        AVCodecParameters *codecParameters = m_format_ctx->streams[m_stream_index]->codecpar;

        //6.获取解码器
        m_codec = avcodec_find_decoder(codecParameters->codec_id);
        if (m_codec == nullptr) {
            LOGD("BaseDecoder::InitFFDecoder avcodec_find_decoder fail.");
            result = -1;
            break;
        }

        //7.创建解码器上下文
        m_codec_ctx = avcodec_alloc_context3(m_codec);
        if (avcodec_parameters_to_context(m_codec_ctx, codecParameters) != 0) {
            LOGD("BaseDecoder::InitFFDecoder avcodec_parameters_to_context fail.");
            result = -1;
            break;
        }

        AVDictionary *avDictionary = nullptr;
        av_dict_set(&avDictionary, "buffer_size", "1024000", 0);
        av_dict_set(&avDictionary, "stimeout", "20000000", 0);
        av_dict_set(&avDictionary, "max_delay", "30000000", 0);
        av_dict_set(&avDictionary, "rtsp_transport", "tcp", 0);

        //8.打开解码器
        if (avcodec_open2(m_codec_ctx, m_codec, &avDictionary) != 0) {
            LOGD("BaseDecoder::InitFFDecoder avcodec_open2 fail");
            result = -1;
            break;
        }
        m_duration = ((float) m_format_ctx->duration / AV_TIME_BASE * 1000);//us to ms

        //创建 AVPacket 存放解码前的数据
        m_packet = av_packet_alloc();
        //创建 AVFrame 存放解码后的数据
        m_frame = av_frame_alloc();
    } while (false);
    if (m_msg_ctx && m_msg_callback) {
        if (result == -1) {
            m_msg_callback(m_msg_ctx, MSG_DECODER_INIT_ERROR, 0);
        } else {
            m_msg_callback(m_msg_ctx, MSG_DECODER_PREPARE, GetDuration());
        }

    }
    return result;
}

void BaseDecoder::UnInitFFDecoder() {
    // 释放缓存
    if (m_packet) {
        av_packet_free(&m_packet);
        m_packet = nullptr;
    }
    if (m_frame) {
        av_frame_free(&m_frame);
        m_frame = nullptr;
    }

    // 关闭解码器
    if (m_codec_ctx) {
        avcodec_close(m_codec_ctx);
        avcodec_free_context(&m_codec_ctx);
        m_codec_ctx = nullptr;
        m_codec = nullptr;
    }

    // 关闭输入流
    if (m_format_ctx) {
        avformat_close_input(&m_format_ctx);
        avformat_free_context(m_format_ctx);
        m_format_ctx = nullptr;
    }
}

void BaseDecoder::StartDecodingThread() {
    // 使用智能指针，线程结束时，自动删除本类指针
    shared_ptr<BaseDecoder> that(this);
    // 创建线程并启动
    thread t(DoAVDecoding, that);
    t.detach();
}

void BaseDecoder::DoAVDecoding(std::shared_ptr<BaseDecoder> that) {
    if (that->InitFFDecoder() == 0) {
        that->OnDecoderReady();
        that->LoopDecode();
    }
    that->OnDecoderDone();
    that->UnInitFFDecoder();
}

void BaseDecoder::LoopDecode() {
    LOGI("BaseDecoder::DecodingLoop start, MediaType=%d", m_media_type)
    if (m_state == STOP) {
        m_state = READY;
    }

    while (1) {
        if (m_state == PAUSE || m_state == FINISH) {
            unique_lock<mutex> lock(m_mutex);
            m_cond.wait(lock);
            m_started_t = GetSysCurrentTime() - m_current_t;
        }

        if (m_state == STOP) {
            break;
        }

        if (m_started_t == -1) {
            m_started_t = GetSysCurrentTime();
        }

        if (DecodeOnePacket() == 0) {
            if (m_state == READY) {
                m_state = PAUSE;
                if (m_msg_ctx && m_msg_callback) {
                    m_msg_callback(m_msg_ctx, MSG_DECODER_PAUSE, 0);
                }
            }
        } else {
            m_state = FINISH;
            if (m_msg_ctx && m_msg_callback) {
                m_msg_callback(m_msg_ctx, MSG_DECODER_FINISH, 0);
            }
        }
    }
    LOGI("BaseDecoder::DecodingLoop end, MediaType=%d", m_media_type)
}

int BaseDecoder::DecodeOnePacket() {
    if (m_seek_position >= 0) {
        //seek to frame
        int64_t seek_target = static_cast<int64_t>(m_seek_position * 1000000);//微秒
        int64_t seek_min = INT64_MIN;
        int64_t seek_max = INT64_MAX;
        int seek_ret = avformat_seek_file(m_format_ctx, -1, seek_min, seek_target, seek_max, 0);
        if (seek_ret < 0) {
            m_seek_success = false;
            LOGE("BaseDecoder::DecodeOneFrame error while seeking MediaType=%d", m_media_type);
        } else {
            if (-1 != m_stream_index) {
                avcodec_flush_buffers(m_codec_ctx);
            }
            ClearCache();
            m_seek_success = true;
            LOGI("BaseDecoder::DecodeOneFrame seekFrame pos=%d, MediaType=%d", m_seek_position, m_media_type);
        }
    }

    int result = av_read_frame(m_format_ctx, m_packet);
    while (result == 0) {
        if (m_packet->stream_index == m_stream_index) {
            switch (avcodec_send_packet(m_codec_ctx, m_packet)) {
                case AVERROR_EOF: {
                    av_packet_unref(m_packet);
                    LOGE("BaseDecoder::Decode error: %s, MediaType=%d", av_err2str(AVERROR_EOF), m_media_type);
                    return -1; //解码结束
                }
                case AVERROR(EAGAIN):
                    LOGE("BaseDecoder::Decode error: %s, MediaType=%d", av_err2str(AVERROR(EAGAIN)), m_media_type);
                    break;
                case AVERROR(EINVAL):
                    LOGE("BaseDecoder::Decode error: %s, MediaType=%d", av_err2str(AVERROR(EINVAL)), m_media_type);
                    break;
                case AVERROR(ENOMEM):
                    LOGE("BaseDecoder::Decode error: %s, MediaType=%d", av_err2str(AVERROR(ENOMEM)), m_media_type);
                    break;
                default:
                    break;
            }

            // 这里需要考虑一个packet有可能包含多个frame的情况
            int frameCount = 0;
            while (avcodec_receive_frame(m_codec_ctx, m_frame) == 0) {
                //更新时间戳
                UpdateTimeStamp();
                //同步
                AVSync();
                //渲染
                OnFrameAvailable(m_frame);
                frameCount++;

                if (m_msg_ctx && m_msg_callback) {
                    int current_time = m_current_t / 1000;
                    if (current_time != m_progress_t) {
                        m_progress_t = current_time;
                        m_msg_callback(m_msg_ctx, MSG_DECODER_TIME, m_progress_t);
                    }
                }
            }
            //判断一个 packet 是否解码完成
            if (frameCount > 0) {
                av_packet_unref(m_packet);
                return 0;
            }

        }
        av_packet_unref(m_packet);
        result = av_read_frame(m_format_ctx, m_packet);
    }
    av_packet_unref(m_packet);
    return -1;
}

void BaseDecoder::UpdateTimeStamp() {
    if (m_frame->pkt_dts != AV_NOPTS_VALUE) {
        m_current_t = m_packet->dts;
    } else if (m_frame->pts != AV_NOPTS_VALUE) {
        m_current_t = m_frame->pts;
    } else {
        m_current_t = 0;
    }
    m_current_t = (int64_t) ((m_current_t * av_q2d(m_format_ctx->streams[m_stream_index]->time_base)) * 1000);

    if (m_seek_position >= 0 && m_seek_success) {
        m_started_t = GetSysCurrentTime() - m_current_t;
        m_seek_position = -1;
        m_seek_success = false;
    }
}

void BaseDecoder::AVSync() {
    //基于系统时钟计算从开始播放流逝的时间
    int64_t elapsedTime = GetSysCurrentTime() - m_started_t;
    //向系统时钟同步
    if (m_current_t > elapsedTime) {
        av_usleep((unsigned int) ((m_current_t - elapsedTime) * 1000));
    }
}
