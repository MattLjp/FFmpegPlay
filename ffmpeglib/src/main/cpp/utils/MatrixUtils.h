
//
// Created by liaojp on 2023/2/27.
//

#ifndef FFMPEGPLAY_MATRIXUTILS_H
#define FFMPEGPLAY_MATRIXUTILS_H

#include <detail/type_mat.hpp>
#include <detail/type_mat4x4.hpp>
#include <gtc/matrix_transform.hpp>

#define TYPE_FITXY  0
#define TYPE_CENTERCROP  1
#define TYPE_CENTERINSIDE  2
#define TYPE_FITSTART  3
#define TYPE_FITEND  4

class MatrixUtils {
public:
    static void GetMatrix(glm::mat4 &matrix, int type, int imgWidth, int imgHeight, int viewWidth, int viewHeight) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            glm::mat4 projection;
            glm::mat4 camera = glm::lookAt(glm::vec3(0, 0, 1), glm::vec3(0, 0, 0), glm::vec3(0, 1, 0));
            if (type == TYPE_FITXY) {
                projection = glm::ortho(-1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 3.0f);
            } else {
                float imgRatio = (float) imgWidth / (float) imgHeight;
                float viewRatio = (float) viewWidth / (float) viewHeight;
                if (imgRatio > viewRatio) {
                    switch (type) {
                        case TYPE_CENTERCROP:
                            projection = glm::ortho(-viewRatio / imgRatio, viewRatio / imgRatio, -1.0f, 1.0f,
                                                    1.0f, 3.0f);
                            break;
                        case TYPE_CENTERINSIDE:
                            projection = glm::ortho(-1.0f, 1.0f, -imgRatio / viewRatio, imgRatio / viewRatio,
                                                    1.0f, 3.0f);
                            break;
                        case TYPE_FITSTART:
                            projection = glm::ortho(-1.0f, 1.0f, 1 - 2 * imgRatio / viewRatio, 1.0f,
                                                    1.0f, 3.0f);
                            break;
                        case TYPE_FITEND:
                            projection = glm::ortho(-1.0f, 1.0f, -1.0f, 2 * imgRatio / viewRatio - 1,
                                                    1.0f, 3.0f);
                            break;
                    }
                } else {
                    switch (type) {
                        case TYPE_CENTERCROP:
                            projection = glm::ortho(-1.0f, 1.0f, -imgRatio / viewRatio, imgRatio / viewRatio,
                                                    1.0f, 3.0f);
                            break;
                        case TYPE_CENTERINSIDE:
                            projection = glm::ortho(-viewRatio / imgRatio, viewRatio / imgRatio, -1.0f, 1.0f,
                                                    1.0f, 3.0f);
                            break;
                        case TYPE_FITSTART:
                            projection = glm::ortho(-1.0f, 2 * viewRatio / imgRatio - 1, -1.0f, 1.0f,
                                                    1.0f, 3.0f);
                            break;
                        case TYPE_FITEND:
                            projection = glm::ortho(1 - 2 * viewRatio / imgRatio, 1.0f, -1.0f, 1.0f,
                                                    1.0f, 3.0f);
                            break;
                    }
                }
            }
            matrix = projection * camera;
        }
    }

    static glm::mat4 GetOriginalMatrix() {
        return glm::mat4(1.0f);
    }
};

#endif //FFMPEGPLAY_MATRIXUTILS_H
