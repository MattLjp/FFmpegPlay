package com.matt.ffmpegplay

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpeglib.NativeRender
import com.matt.ffmpeglib.NativeRender.Companion.IMAGE_FORMAT_NV21
import com.matt.ffmpegplay.databinding.ActivityNativeOpenglBinding
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class NativeOpenglActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var binding: ActivityNativeOpenglBinding
    private val nativeRender by lazy { NativeRender() }
    private val bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.a, BitmapFactory.Options().apply { inScaled = false })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeOpenglBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.surfaceView.holder.addCallback(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_native, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn1 -> {
                nativeRender.setDrawerType(NativeRender.SAMPLE_TYPE_KEY_TRIANGLE)
            }
            R.id.btn2 -> {
                nativeRender.setDrawerType(NativeRender.SAMPLE_TYPE_KEY_IMAGE)
                val byteArray = ByteBuffer.allocate(bitmap.byteCount)
                bitmap.copyPixelsToBuffer(byteArray)
                nativeRender.setImageData(
                    NativeRender.IMAGE_FORMAT_RGBA,
                    bitmap.width,
                    bitmap.height,
                    byteArray.array()
                )
            }
            R.id.btn3 -> {
                nativeRender.setDrawerType(NativeRender.SAMPLE_TYPE_KEY_YUV_TEXTURE_MAP)
                loadNV21Image()
            }
            R.id.btn4 -> {
                nativeRender.setDrawerType(NativeRender.SAMPLE_TYPE_KEY_TRIANGLE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        nativeRender.onInit(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        nativeRender.onSurfaceChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        nativeRender.onUnInit()
    }

    private fun loadNV21Image() {
        var `is`: InputStream? = null
        try {
            `is` = assets.open("YUV_Image_840x1074.NV21")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var lenght = 0
        try {
            lenght = `is`!!.available()
            val buffer = ByteArray(lenght)
            `is`.read(buffer)
            nativeRender.setImageData(IMAGE_FORMAT_NV21, 840, 1074, buffer)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}