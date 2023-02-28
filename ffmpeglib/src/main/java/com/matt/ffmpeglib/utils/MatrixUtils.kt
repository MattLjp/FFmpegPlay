package com.matt.ffmpeglib.utils

import android.opengl.Matrix

/**
 * Created by Liaojp on 2023/2/8.
 */
object MatrixUtils {
    const val TYPE_FITXY = 0
    const val TYPE_CENTERCROP = 1
    const val TYPE_CENTERINSIDE = 2
    const val TYPE_FITSTART = 3
    const val TYPE_FITEND = 4

    /**
     * 通过传入图片宽高和预览宽高，计算变换矩阵
     * @param matrix FloatArray
     * @param type Int
     * @param imgWidth Int
     * @param imgHeight Int
     * @param viewWidth Int
     * @param viewHeight Int
     */
    fun getMatrix(matrix: FloatArray, type: Int, imgWidth: Int, imgHeight: Int, viewWidth: Int, viewHeight: Int) {
        if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
            val projection = FloatArray(16)
            val camera = FloatArray(16)
            if (type == TYPE_FITXY) {
                Matrix.orthoM(projection, 0, -1f, 1f, -1f, 1f, 1f, 3f)
                Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
                Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
                return
            }
            val imgRatio = imgWidth.toFloat() / imgHeight
            val viewRatio = viewWidth.toFloat() / viewHeight
            if (imgRatio > viewRatio) {
                when (type) {
                    TYPE_CENTERCROP -> Matrix.orthoM(
                        projection, 0, -viewRatio / imgRatio, viewRatio / imgRatio,
                        -1f, 1f, 1f, 3f
                    )
                    TYPE_CENTERINSIDE -> Matrix.orthoM(
                        projection, 0, -1f, 1f,
                        -imgRatio / viewRatio, imgRatio / viewRatio, 1f, 3f
                    )
                    TYPE_FITSTART -> Matrix.orthoM(
                        projection, 0, -1f, 1f,
                        1 - 2 * imgRatio / viewRatio, 1f, 1f, 3f
                    )
                    TYPE_FITEND -> Matrix.orthoM(
                        projection, 0, -1f, 1f,
                        -1f, 2 * imgRatio / viewRatio - 1, 1f, 3f
                    )
                }
            } else {
                when (type) {
                    TYPE_CENTERCROP -> Matrix.orthoM(
                        projection, 0, -1f, 1f,
                        -imgRatio / viewRatio, imgRatio / viewRatio, 1f, 3f
                    )
                    TYPE_CENTERINSIDE -> Matrix.orthoM(
                        projection, 0, -viewRatio / imgRatio, viewRatio / imgRatio,
                        -1f, 1f, 1f, 3f
                    )
                    TYPE_FITSTART -> Matrix.orthoM(
                        projection, 0, -1f, 2 * viewRatio / imgRatio - 1,
                        -1f, 1f, 1f, 3f
                    )
                    TYPE_FITEND -> Matrix.orthoM(
                        projection, 0, 1 - 2 * viewRatio / imgRatio, 1f,
                        -1f, 1f, 1f, 3f
                    )
                }
            }
            Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
            Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
        }
    }

    fun getOriginalMatrix(): FloatArray {
        return floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }

    /**
     * 旋转
     * @param m FloatArray
     * @param angle Float
     * @return FloatArray
     */
    fun rotate(m: FloatArray, angle: Float): FloatArray {
        Matrix.rotateM(m, 0, angle, 0f, 0f, 1f)
        return m
    }

    /**
     * 镜像
     * @param m FloatArray
     * @param x Boolean
     * @param y Boolean
     * @return FloatArray
     */
    fun flip(m: FloatArray, x: Boolean, y: Boolean): FloatArray {
        if (x || y) {
            Matrix.scaleM(m, 0, if (x) -1f else 1f, if (y) -1f else 1f, 1f)
        }
        return m
    }

}