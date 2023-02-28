package com.matt.ffmpeglib.obj

import android.content.res.Resources
import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

/**
 * Created by Liaojp on 20/2/2023.
 */
object ObjLoaderUtil {
    private const val TAG = "ObjLoaderUtil"

    /**
     * 解析
     */
    fun load(res: Resources?, fname: String?): List<ObjInfo> {
        Log.d(TAG, "---loadObj---")
        // 返回的数据列表
        val objectList = mutableListOf<ObjInfo>()
        if (res == null || fname.isNullOrEmpty()) {
            return objectList
        }
        /**
         * 所有顶点信息
         */
        // 顶点数据
        val vertices = mutableListOf<Float>()
        // 纹理数据
        val texCoords = mutableListOf<Float>()
        // 法向量数据
        val normals = mutableListOf<Float>()
        // 全部材质列表
        var mtlMap: Map<String?, MtlInfo>? = null

        // Ojb索引数据
        var currObjInfo = ObjInfo()
        // 当前材质名称
        var currMaterialName: String? = null
        // 是否有面数据的标识
        var currObjHasFaces = false
        try {
            // 每一行的信息
            var line: String? = null
            // 读取assets下文件
            val `in` = res.assets.open(fname)
            val isr = InputStreamReader(`in`)
            val buffer = BufferedReader(isr)

            // 循环读取每一行的数据
            while (buffer.readLine().also { line = it } != null) {
                // 忽略 空行和注释
                if (line!!.isEmpty() || (line!![0] == '#')) {
                    continue
                }
                // 以空格分割String
                var parts = StringTokenizer(line, " ")
                val numTokens = parts.countTokens()
                if (numTokens == 0) {
                    continue
                }
                // 打头的字符
                val type = parts.nextToken()
                when (type) {
                    MTLLIB -> {
                        // 材质
                        if (!parts.hasMoreTokens()) {
                            continue
                        }
                        // 需要重命名材质文件,暂定同一路径下(goku/goku.mtl)
                        val materialLibPath = parts.nextToken()
                        if (!TextUtils.isEmpty(materialLibPath)) {
                            mtlMap = MtlLoaderUtil.load(res, materialLibPath)
                        }
                    }
                    O -> {
                        // 对象名称
                        val objName = if (parts.hasMoreTokens()) parts.nextToken() else "def"
                        // 面数据
                        if (currObjHasFaces) {
                            // 添加到数组中
                            objectList.add(currObjInfo)
                            // 创建新的索引对象
                            currObjInfo = ObjInfo()
                            currObjHasFaces = false
                        }
                        currObjInfo.name = objName
                        // 对应材质
                        if (!TextUtils.isEmpty(currMaterialName) && mtlMap != null) {
                            currObjInfo.mtlData = mtlMap[currMaterialName]
                        }
                    }
                    V -> {
                        //顶点
                        vertices.add(parts.nextToken().toFloat())
                        vertices.add(parts.nextToken().toFloat())
                        vertices.add(parts.nextToken().toFloat())
                    }
                    VT -> {
                        // 纹理
                        // 这里纹理的Y值，需要(Y = 1-Y0),原因是openGl的纹理坐标系与android的坐标系存在Y值镜像的状态
                        texCoords.add(parts.nextToken().toFloat())
                        texCoords.add(1f - parts.nextToken().toFloat())
                    }
                    VN -> {
                        // 法向量
                        normals.add(parts.nextToken().toFloat())
                        normals.add(parts.nextToken().toFloat())
                        normals.add(parts.nextToken().toFloat())
                    }
                    USEMTL -> {
                        // 使用材质
                        // 材质名称
                        currMaterialName = parts.nextToken()
                        if (currObjHasFaces) {
                            // 添加到数组中
                            objectList.add(currObjInfo)
                            // 创建一个index对象
                            currObjInfo = ObjInfo()
                            currObjHasFaces = false
                        }
                        // 材质名称
                        if (!TextUtils.isEmpty(currMaterialName) && mtlMap != null) {
                            currObjInfo.mtlData = mtlMap[currMaterialName]
                        }
                    }
                    F -> {
                        // "f"面属性  索引数组
                        // 当前obj对象有面数据
                        currObjHasFaces = true
                        // 是否为矩形(android 均为三角形，这里暂时先忽略多边形的情况)
                        val isQuad = numTokens == 5
                        val quadvids = IntArray(4)
                        val quadtids = IntArray(4)
                        val quadnids = IntArray(4)

                        // 如果含有"//" 替换
                        val emptyVt = line!!.indexOf("//") > -1
                        if (emptyVt) {
                            line = line!!.replace("//", "/")
                        }
                        // "f 103/1/1 104/2/1 113/3/1"以" "分割
                        parts = StringTokenizer(line)
                        // “f”
                        parts.nextToken()
                        // "103/1/1 104/2/1 113/3/1"再以"/"分割
                        var subParts = StringTokenizer(parts.nextToken(), "/")
                        val partLength = subParts.countTokens()

                        // 纹理数据
                        val hasuv = partLength >= 2 && !emptyVt
                        // 法向量数据
                        val hasn = partLength == 3 || partLength == 2 && emptyVt
                        // 索引index
                        var idx: Int
                        var i = 1
                        while (i < numTokens) {
                            if (i > 1) {
                                subParts = StringTokenizer(parts.nextToken(), "/")
                            }
                            // 顶点索引
                            idx = subParts.nextToken().toInt()
                            if (idx < 0) {
                                idx += vertices.size / 3
                            } else {
                                idx -= 1
                            }
                            if (!isQuad) {
                                currObjInfo.vertexIndices.add(idx)
                            } else {
                                quadvids[i - 1] = idx
                            }
                            // 纹理索引
                            if (hasuv) {
                                idx = subParts.nextToken().toInt()
                                if (idx < 0) {
                                    idx += texCoords.size / 2
                                } else {
                                    idx -= 1
                                }
                                if (!isQuad) {
                                    currObjInfo.texCoordIndices.add(idx)
                                } else {
                                    quadtids[i - 1] = idx
                                }
                            }
                            // 法向量数据
                            if (hasn) {
                                idx = subParts.nextToken().toInt()
                                if (idx < 0) {
                                    idx += normals.size / 3
                                } else {
                                    idx -= 1
                                }
                                if (!isQuad) {
                                    currObjInfo.normalIndices.add(idx)
                                } else {
                                    quadnids[i - 1] = idx
                                }
                            }
                            i++
                        }
                        // 如果是多边形
                        if (isQuad) {
                            val indices = intArrayOf(0, 1, 2, 0, 2, 3)
                            var i = 0
                            while (i < 6) {
                                val index = indices[i]
                                currObjInfo.vertexIndices.add(quadvids[index])
                                currObjInfo.texCoordIndices.add(quadtids[index])
                                currObjInfo.normalIndices.add(quadnids[index])
                                ++i
                            }
                        }
                    }
                    else -> {}
                }
            }
            //
            buffer.close()
            // 存在索引面数据，添加到index列表中
            if (currObjHasFaces) {
                // 添加到数组中
                objectList.add(currObjInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //###############################顶点、法向量、纹理一一对应#################################
        // 循环索引对象列表
        val numObjects = objectList.size
        for (j in 0 until numObjects) {
            val ObjInfo = objectList[j]
            var i: Int
            // 顶点数据 初始化
            val aVertices = FloatArray(ObjInfo.vertexIndices.size * 3)
            // 顶点纹理数据 初始化
            val aTexCoords = FloatArray(ObjInfo.texCoordIndices.size * 2)
            // 顶点法向量数据 初始化
            val aNormals = FloatArray(ObjInfo.normalIndices.size * 3)
            // 按照索引，重新组织顶点数据
            i = 0
            while (i < ObjInfo.vertexIndices.size) {

                // 顶点索引，三个一组做为一个三角形
                val faceIndex = ObjInfo.vertexIndices[i] * 3
                val vertexIndex = i * 3
                try {
                    // 按照索引，重新组织顶点数据
                    aVertices[vertexIndex] = vertices[faceIndex]
                    aVertices[vertexIndex + 1] = vertices[faceIndex + 1]
                    aVertices[vertexIndex + 2] = vertices[faceIndex + 2]
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ++i
            }
            // 按照索引组织 纹理数据
            if (texCoords.size > 0) {
                i = 0
                while (i < ObjInfo.texCoordIndices.size) {
                    val texCoordIndex = ObjInfo.texCoordIndices[i] * 2
                    val ti = i * 2
                    aTexCoords[ti] = texCoords[texCoordIndex]
                    aTexCoords[ti + 1] = texCoords[texCoordIndex + 1]
                    ++i
                }
            }
            // 按照索引组织 法向量数据
            i = 0
            while (i < ObjInfo.normalIndices.size) {
                val normalIndex = ObjInfo.normalIndices[i] * 3
                val ni = i * 3
                if (normals.size == 0) {
                    throw Exception("There are no normals specified for this model. Please re-export with normals.")
                }
                aNormals[ni] = normals[normalIndex]
                aNormals[ni + 1] = normals[normalIndex + 1]
                aNormals[ni + 2] = normals[normalIndex + 2]
                ++i
            }
            // 数据设置到oid.targetObj中
            ObjInfo.aVertices = aVertices
            ObjInfo.aTexCoords = aTexCoords
            ObjInfo.aNormals = aNormals
            //
            ObjInfo.vertexIndices.clear()
            ObjInfo.texCoordIndices.clear()
            ObjInfo.normalIndices.clear()
        }
        return objectList
    }

    /**
     * obj需解析字段
     */
    // obj对应的材质文件
    private const val MTLLIB = "mtllib"

    // 组名称
    private const val G = "g"

    // o 对象名称(Object name)
    private const val O = "o"

    // 顶点
    private const val V = "v"

    // 纹理坐标
    private const val VT = "vt"

    // 顶点法线
    private const val VN = "vn"

    // 使用的材质
    private const val USEMTL = "usemtl"

    // v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3(索引起始于1)
    private const val F = "f"


}