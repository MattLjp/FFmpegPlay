package com.matt.ffmpeglib.utils

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by Liaojp on 2023/2/6.
 */
object ResReadUtils {

    /**
     * 读取资源
     * @param context Context
     * @param resourceId Int
     * @return String
     */
    fun readResource(context: Context, resourceId: Int): String {
        val builder = StringBuilder()
        try {
            val inputStream: InputStream = context.resources.openRawResource(resourceId)
            val streamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(streamReader)
            var textLine: String?
            while (bufferedReader.readLine().also { textLine = it } != null) {
                builder.append(textLine)
                builder.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
        return builder.toString()
    }

}