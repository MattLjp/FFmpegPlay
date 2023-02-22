//
// Created by liaojp on 21/2/2023.
//
#include <jni.h>
#include "SimpleRender.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_matt_opengllib_render_NativeRender_onInit(JNIEnv *env, jobject thiz) {
    SimpleRender::GetInstance();
}

JNIEXPORT void JNICALL
Java_com_matt_opengllib_render_NativeRender_onUnInit(JNIEnv *env, jobject thiz) {
    SimpleRender::DestroyInstance();
}

JNIEXPORT void JNICALL
Java_com_matt_opengllib_render_NativeRender_setDrawerType(JNIEnv *env, jobject thiz, jint type) {
    SimpleRender::GetInstance()->SetDrawerType(type);
}

JNIEXPORT void JNICALL
Java_com_matt_opengllib_render_NativeRender_setImageData(JNIEnv *env, jobject thiz, jint format, jint width,
                                                         jint height, jbyteArray bytes) {
    int len = env->GetArrayLength(bytes);
    uint8_t *buf = new uint8_t[len];
    env->GetByteArrayRegion(bytes, 0, len, reinterpret_cast<jbyte *>(buf));
    SimpleRender::GetInstance()->SetImageData(format, width, height, buf);
    delete[] buf;
    env->DeleteLocalRef(bytes);
}

JNIEXPORT void JNICALL
Java_com_matt_opengllib_render_NativeRender_onSurfaceCreated(JNIEnv *env, jobject thiz) {
    SimpleRender::GetInstance()->OnSurfaceCreated();
}

JNIEXPORT void JNICALL
Java_com_matt_opengllib_render_NativeRender_onSurfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    SimpleRender::GetInstance()->OnSurfaceChanged(width, height);
}

JNIEXPORT void JNICALL
Java_com_matt_opengllib_render_NativeRender_onDrawFrame(JNIEnv *env, jobject thiz) {
    SimpleRender::GetInstance()->OnDrawFrame();
}

#ifdef __cplusplus
}
#endif

