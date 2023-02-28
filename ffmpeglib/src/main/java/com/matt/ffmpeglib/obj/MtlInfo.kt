package com.matt.ffmpeglib.obj

/**
 * Created by Liaojp on 20/2/2023.
 * mtl文件信息保存类
 */
class MtlInfo {
    // 材质对象名称
    var name: String? = null

    // 环境光
    var Ka_Color = 0

    // 散射光
    var Kd_Color = 0

    // 镜面光
    var Ks_Color = 0

    // 高光调整参数
    var ns = 0f

    // 溶解度，为0时完全透明，1完全不透明
    var alpha = 1f

    // map_Ka，map_Kd，map_Ks：材质的环境（ambient），散射（diffuse）和镜面（specular）贴图
    var Ka_Texture: String? = null
    var Kd_Texture: String? = null
    var Ks_ColorTexture: String? = null
    var Ns_Texture: String? = null
    var alphaTexture: String? = null
    var bumpTexture: String? = null
}