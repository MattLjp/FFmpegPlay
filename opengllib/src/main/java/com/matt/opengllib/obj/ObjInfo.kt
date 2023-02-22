package com.matt.opengllib.obj

/**
 * Created by Liaojp on 20/2/2023.
 * obj文件信息类
 */
class ObjInfo {
    /**
     * 对象名称
     */
    var name: String? = null

    /**
     * 材质
     */
    var mtlData: MtlInfo? = null

    /**
     * 顶点、纹理、法向量一一对应后的数据
     */
    var aVertices: FloatArray? = null

    // 顶点纹理可能会没有
    var aTexCoords: FloatArray? = null
    var aNormals: FloatArray? = null

    /**
     * index数组(顶点、纹理、法向量一一对应后，以下三个列表会清空)
     */
    // 顶点index数组
    var vertexIndices = mutableListOf<Int>()

    // 纹理index数组
    var texCoordIndices = mutableListOf<Int>()

    // 法向量index数组
    var normalIndices = mutableListOf<Int>()
    override fun toString(): String {
        return "ObjInfo(name=$name, mtlData=$mtlData, aVertices=${aVertices?.contentToString()}, aTexCoords=${aTexCoords?.contentToString()}, aNormals=${aNormals?.contentToString()}, vertexIndices=$vertexIndices, texCoordIndices=$texCoordIndices, normalIndices=$normalIndices)"
    }


}
