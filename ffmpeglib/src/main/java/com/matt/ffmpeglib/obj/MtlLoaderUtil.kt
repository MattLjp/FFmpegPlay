package com.matt.ffmpeglib.obj

import android.content.res.Resources
import android.graphics.Color
import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

/**
 * Created by Liaojp on 20/2/2023.
 * mtl文件解析类
 */
object MtlLoaderUtil {

    private const val TAG = "MtlLoaderUtil"


    /**
     * 加载材质的方法
     *
     * @param fname assets的mtl文件路径
     * @param res
     * @return
     */
    fun load(res: Resources?, fname: String?): Map<String?, MtlInfo> {
        // 材质数组
        val mMTLMap = mutableMapOf<String?, MtlInfo>()
        //
        if (res == null || TextUtils.isEmpty(fname)) {
            return mMTLMap
        }
        //
        var currMtlInfo: MtlInfo? = null
        try {
            // 读取assets下文件
            val `in` = res.assets.open(fname!!)
            val isr = InputStreamReader(`in`)
            val buffer = BufferedReader(isr)
            // 行数据
            var line: String
            //
            while (buffer.readLine().also { line = it } != null) {
                // Skip comments and empty lines.
                if (line.isEmpty() || line[0] == '#') {
                    continue
                }
                //
                val parts = StringTokenizer(line, " ")
                val numTokens = parts.countTokens()
                if (numTokens == 0) {
                    continue
                }
                //
                var type = parts.nextToken()
                type = type.replace("\\t".toRegex(), "")
                type = type.replace(" ".toRegex(), "")
                when (type) {
                    NEWMTL -> {
                        // 定义一个名为 'xxx'的材质
                        val name = if (parts.hasMoreTokens()) parts.nextToken() else "def"
                        // 将上一个对象加入到列表中
                        if (currMtlInfo != null) {
                            mMTLMap[currMtlInfo.name] = currMtlInfo
                        }
                        // 创建材质对象
                        currMtlInfo = MtlInfo()
                        // 材质对象名称
                        currMtlInfo.name = name
                    }
                    KA ->                         // 环境光
                        currMtlInfo!!.Ka_Color = getColorFromParts(parts)
                    KD ->                         // 散射光
                        currMtlInfo!!.Kd_Color = getColorFromParts(parts)
                    KS ->                         // 镜面光
                        currMtlInfo!!.Ks_Color = getColorFromParts(parts)
                    NS -> {
                        // 高光调整参数
                        val ns = parts.nextToken()
                        currMtlInfo!!.ns = ns.toFloat()
                    }
                    D ->                         // 溶解度，为0时完全透明，1完全不透明
                        currMtlInfo!!.alpha = parts.nextToken().toFloat()
                    MAP_KA -> currMtlInfo!!.Ka_Texture = parts.nextToken()
                    MAP_KD -> currMtlInfo!!.Kd_Texture = parts.nextToken()
                    MAP_KS -> currMtlInfo!!.Ks_ColorTexture = parts.nextToken()
                    MAP_NS -> currMtlInfo!!.Ns_Texture = parts.nextToken()
                    MAP_D, MAP_TR -> currMtlInfo!!.alphaTexture = parts.nextToken()
                    MAP_BUMP -> currMtlInfo!!.bumpTexture = parts.nextToken()
                    else -> {}
                }
            }
            if (currMtlInfo != null) {
                mMTLMap[currMtlInfo.name] = currMtlInfo
            }
            buffer.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
        }
        return mMTLMap
    }

    //####################################################################################
    //####################################################################################
    /**
     * 材质需解析字段
     */
    // 定义一个名为 'xxx'的材质
    private const val NEWMTL = "newmtl"

    // 材质的环境光（ambient color）
    private const val KA = "Ka"

    // 散射光（diffuse color）用Kd
    private const val KD = "Kd"

    // 镜面光（specular color）用Ks
    private const val KS = "Ks"

    // 反射指数 定义了反射高光度。该值越高则高光越密集，一般取值范围在0~1000。
    private const val NS = "Ns"

    // 渐隐指数描述 参数factor表示物体融入背景的数量，取值范围为0.0~1.0，取值为1.0表示完全不透明，取值为0.0时表示完全透明。
    private const val D = "d"

    // 滤光透射率
    private const val TR = "Tr"

    // map_Ka，map_Kd，map_Ks：材质的环境（ambient），散射（diffuse）和镜面（specular）贴图
    private const val MAP_KA = "map_Ka"
    private const val MAP_KD = "map_Kd"
    private const val MAP_KS = "map_Ks"
    private const val MAP_NS = "map_Ns"
    private const val MAP_D = "map_d"
    private const val MAP_TR = "map_Tr"
    private const val MAP_BUMP = "map_Bump"

    /**
     * 返回一个oxffffffff格式的颜色值
     *
     * @param parts
     * @return
     */
    private fun getColorFromParts(parts: StringTokenizer): Int {
        val r = (parts.nextToken().toFloat() * 255f).toInt()
        val g = (parts.nextToken().toFloat() * 255f).toInt()
        val b = (parts.nextToken().toFloat() * 255f).toInt()
        return Color.rgb(r, g, b)
    }

}