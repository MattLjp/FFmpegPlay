//
// Created by Liaojp on 2023/1/8.
//

#include "player.h"

Player::~Player() {
    UnInit();
}

void Player::Init(JNIEnv *jniEnv, jobject obj, char *url, jobject surface) {
    jniEnv->GetJavaVM(&m_JavaVM);
    m_JavaObj = jniEnv->NewGlobalRef(obj);

    // 解码器
    m_video_decoder = new VideoDecoder(url);
    m_audio_decoder = new AudioDecoder(url);

    // 渲染器
//    m_video_render = new NativeRender(jniEnv, surface);
    m_drawer = new VideoDrawer();
    m_proxyImpl = new DrawerProxyImpl();
    m_proxyImpl->AddDrawer(m_drawer);
    m_video_render = new OpenGLRender(jniEnv, m_proxyImpl, surface);
    m_video_decoder->SetVideoRender(m_video_render);

    m_audio_render = new OpenSLRender();
    m_audio_decoder->SetAudioRender(m_audio_render);

    m_video_decoder->SetMessageCallback(this, PostMessage);
}

void Player::UnInit() {
    // 此处不需要 delete 成员指针
    // 在BaseDecoder中的线程已经使用智能指针，会自动释放
    if (m_video_decoder) {
        m_video_decoder->Stop();
    }

    if (m_audio_decoder) {
        m_audio_decoder->Stop();
    }

    bool isAttach = false;
    if (m_JavaObj) {
        GetJNIEnv(&isAttach)->DeleteGlobalRef(m_JavaObj);
        m_JavaObj = nullptr;
    }
    if (isAttach) {
        GetJavaVM()->DetachCurrentThread();
    }
}

void Player::Play() {
    if (m_video_decoder) {
        m_video_decoder->Start();
    }

    if (m_audio_decoder) {
        m_audio_decoder->Start();
    }
}

void Player::Pause() {
    if (m_video_decoder) {
        m_video_decoder->Pause();
    }

    if (m_audio_decoder) {
        m_audio_decoder->Pause();
    }
}

void Player::SeekToPosition(int position) {
    if (m_video_decoder) {
        m_video_decoder->SeekToPosition(position);
    }

    if (m_audio_decoder) {
        m_audio_decoder->SeekToPosition(position);
    }
}

void Player::PostMessage(void *context, int msgType, int msgCode) {
    if (context) {
        Player *player = static_cast<Player *>(context);
        bool isAttach = false;
        JNIEnv *env = player->GetJNIEnv(&isAttach);
        if (env == nullptr) return;

        jobject javaObj = player->GetJavaObj();
        static jmethodID fid_s = nullptr;
        if (fid_s == NULL) {
            fid_s = env->GetMethodID(env->GetObjectClass(javaObj), JAVA_PLAYER_EVENT_CALLBACK_API_NAME, "(II)V");
            if (fid_s == NULL) {
                return; /* exception already thrown */
            }
        }
        env->CallVoidMethod(javaObj, fid_s, msgType, msgCode);
        if (isAttach) {
            player->GetJavaVM()->DetachCurrentThread();
        }
    }

}

JNIEnv *Player::GetJNIEnv(bool *isAttach) {
    JNIEnv *env;
    int status;
    if (m_JavaVM == nullptr) {
        LOGE("Player::GetJNIEnv m_JavaVM == nullptr")
        return nullptr;
    }
    *isAttach = false;
    status = m_JavaVM->GetEnv((void **) &env, JNI_VERSION_1_4);
    if (status != JNI_OK) {
        status = m_JavaVM->AttachCurrentThread(&env, nullptr);
        if (status != JNI_OK) {
            LOGE("Player::GetJNIEnv failed to attach current thread");
            return nullptr;
        }
        *isAttach = true;
    }
    return env;
}


