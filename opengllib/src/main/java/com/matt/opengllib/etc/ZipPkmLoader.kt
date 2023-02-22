package com.matt.opengllib.etc

import android.content.Context
import android.content.res.AssetManager
import android.opengl.ETC1
import android.opengl.ETC1Util
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipInputStream

/**
 * Created by Liaojp on 19/2/2023.
 */
class ZipPkmLoader(private val context: Context) {
    private val manager: AssetManager by lazy { context.assets }
    private var path: String? = null
    private var zipStream: ZipInputStream? = null
    private var headerBuffer: ByteBuffer? = null

    fun setZipPath(path: String) {
        this.path = path
    }

    fun open(): Boolean {
        return if (path == null) false else try {
            if (path!!.startsWith("assets/")) {
                val s: InputStream = manager.open(path!!.substring(7))
                zipStream = ZipInputStream(s)
            } else {
                zipStream = ZipInputStream(FileInputStream(path))
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun close() {
        try {
            zipStream?.closeEntry()
            zipStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        headerBuffer?.clear()
        headerBuffer = null
    }

    private fun hasElements(): Boolean {
        try {
            if (zipStream?.nextEntry != null) {
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    fun getNextTexture(): ETC1Util.ETC1Texture? {
        if (hasElements()) {
            try {
                return createTexture(zipStream!!)
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun createTexture(input: InputStream): ETC1Util.ETC1Texture {
        var width = 0
        var height = 0
        val ioBuffer = ByteArray(4096)
        run {
            if (input.read(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE) != ETC1.ETC_PKM_HEADER_SIZE) {
                throw IOException("Unable to read PKM file header.")
            }
            if (headerBuffer == null) {
                headerBuffer = ByteBuffer.allocateDirect(ETC1.ETC_PKM_HEADER_SIZE)
                    .order(ByteOrder.nativeOrder())
            }
            headerBuffer!!.put(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE).position(0)
            if (!ETC1.isValid(headerBuffer)) {
                throw IOException("Not a PKM file.")
            }
            width = ETC1.getWidth(headerBuffer)
            height = ETC1.getHeight(headerBuffer)
        }
        val encodedSize = ETC1.getEncodedDataSize(width, height)
        val dataBuffer = ByteBuffer.allocateDirect(encodedSize).order(ByteOrder.nativeOrder())
        var len: Int
        while (input.read(ioBuffer).also { len = it } != -1) {
            dataBuffer.put(ioBuffer, 0, len)
        }
        dataBuffer.position(0)
        return ETC1Util.ETC1Texture(width, height, dataBuffer)
    }


}