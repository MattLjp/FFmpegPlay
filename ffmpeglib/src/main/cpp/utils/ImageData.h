//
// Created by liaojp on 2023/2/22.
//

#ifndef FFMPEGPLAY_IMAGEDATA_H
#define FFMPEGPLAY_IMAGEDATA_H

#include <malloc.h>
#include <string.h>
#include <unistd.h>
#include<iostream>
#include "stdio.h"
#include "sys/stat.h"
#include "LogUtil.h"

#define IMAGE_FORMAT_RGBA           0x01
#define IMAGE_FORMAT_NV21           0x02
#define IMAGE_FORMAT_NV12           0x03
#define IMAGE_FORMAT_I420           0x04
#define IMAGE_FORMAT_YUYV           0x05
#define IMAGE_FORMAT_GRAY           0x06
#define IMAGE_FORMAT_I444           0x07
#define IMAGE_FORMAT_P010           0x08

struct ImageData {
    int width;
    int height;
    int format;
    uint8_t *tempData;
    uint8_t *ppPlane[3];

    ImageData() {
        width = 0;
        height = 0;
        format = 0;
        ppPlane[0] = nullptr;
        ppPlane[1] = nullptr;
        ppPlane[2] = nullptr;
    }

    ~ImageData() {
        width = 0;
        height = 0;
        format = 0;
        if (ppPlane[0] != nullptr) {
            free(ppPlane[0]);
        }
        ppPlane[0] = nullptr;
        ppPlane[1] = nullptr;
        ppPlane[2] = nullptr;
    }

};

class NativeImageUtil {
public:
    static void AllocNativeImage(ImageData *pImage) {
        if (pImage->height == 0 || pImage->width == 0) return;
        switch (pImage->format) {
            case IMAGE_FORMAT_RGBA: {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 4));
            }
                break;
            case IMAGE_FORMAT_YUYV: {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 2));
            }
                break;
            case IMAGE_FORMAT_NV12:
            case IMAGE_FORMAT_NV21: {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 1.5));
                pImage->ppPlane[1] = pImage->ppPlane[0] + pImage->width * pImage->height;
            }
                break;
            case IMAGE_FORMAT_I420: {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 1.5));
                pImage->ppPlane[1] = pImage->ppPlane[0] + pImage->width * pImage->height;
                pImage->ppPlane[2] = pImage->ppPlane[1] + pImage->width * (pImage->height >> 2);
            }
                break;
            case IMAGE_FORMAT_GRAY: {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height));
            }
                break;
            case IMAGE_FORMAT_I444: {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 3));
            }
                break;
            case IMAGE_FORMAT_P010: {
                pImage->ppPlane[0] = static_cast<uint8_t *>(malloc(pImage->width * pImage->height * 3));
                pImage->ppPlane[1] = pImage->ppPlane[0] + pImage->width * pImage->height * 2;
            }
                break;
            default:
                LOGE("NativeImageUtil::NativeImageUtil::AllocNativeImage do not support the format. Format = %d",
                     pImage->format);
                break;
        }
    }

    static void CopyNativeImage(ImageData *pDstImg, ImageData *pSrcImg) {
        if (pSrcImg == nullptr || pSrcImg->tempData == nullptr) return;

        pDstImg->width = pSrcImg->width;
        pDstImg->height = pSrcImg->height;
        pDstImg->format = pSrcImg->format;
        AllocNativeImage(pDstImg);

        switch (pSrcImg->format) {
            case IMAGE_FORMAT_I420:
            case IMAGE_FORMAT_NV21:
            case IMAGE_FORMAT_NV12: {
                memcpy(pDstImg->ppPlane[0], pSrcImg->tempData, pSrcImg->width * pSrcImg->height * 1.5);
            }
                break;
            case IMAGE_FORMAT_YUYV: {
                memcpy(pDstImg->ppPlane[0], pSrcImg->tempData, pSrcImg->width * pSrcImg->height * 2);
            }
                break;
            case IMAGE_FORMAT_RGBA: {
                memcpy(pDstImg->ppPlane[0], pSrcImg->tempData, pSrcImg->width * pSrcImg->height * 4);
            }
                break;
            case IMAGE_FORMAT_GRAY: {
                memcpy(pDstImg->ppPlane[0], pSrcImg->tempData, pSrcImg->width * pSrcImg->height);
            }
                break;
            case IMAGE_FORMAT_P010:
            case IMAGE_FORMAT_I444: {
                memcpy(pDstImg->ppPlane[0], pSrcImg->tempData, pSrcImg->width * pSrcImg->height * 3);
            }
                break;
            default: {
                LOGE("NativeImageUtil::CopyNativeImage do not support the format. Format = %d", pSrcImg->format);
            }
                break;
        }

    }

};


#endif //OPENGL_IMAGEDATA_H
