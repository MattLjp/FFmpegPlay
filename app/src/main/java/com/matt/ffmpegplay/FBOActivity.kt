package com.matt.ffmpegplay

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ImageUtils
import com.matt.ffmpeglib.drawer.FBODrawer
import com.matt.ffmpeglib.render.SimpleRender
import com.matt.ffmpegplay.databinding.ActivityGlSurfaceBinding
import java.nio.ByteBuffer

/**
 * Created by Liaojp on 2023/2/7.
 */
class FBOActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGlSurfaceBinding
    private var render = SimpleRender()
    private val drawer by lazy { FBODrawer(this, bitmap) }
    private val bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.a, BitmapFactory.Options().apply { inScaled = false })
    }

    private var isHalf = false
    private var type = 0
    private var data = floatArrayOf(0.0f, 0.0f, 0.0f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGlSurfaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawer.callback = { data, width, height ->
            onCall(data, width, height)
        }
        binding.glSurfaceView.setEGLContextClientVersion(3)
        render.addDrawer(drawer)
        binding.glSurfaceView.setRenderer(render)
        binding.glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mDeal -> {
                isHalf = !isHalf
                if (isHalf) {
                    item.title = "处理一半"
                } else {
                    item.title = "全部处理"
                }
            }
            R.id.mDefault -> {
                type = 0
                data = floatArrayOf(0.0f, 0.0f, 0.0f)
            }
            R.id.mGray -> {
                type = 1
                data = floatArrayOf(0.299f, 0.587f, 0.114f)
            }
            R.id.mCool -> {
                type = 2
                data = floatArrayOf(0.1f, 0.1f, 0.0f)
            }
            R.id.mWarm -> {
                type = 2
                data = floatArrayOf(0.1f, 0.1f, 0.0f)
            }
            R.id.mBlur -> {
                type = 3
                data = floatArrayOf(0.006f, 0.004f, 0.002f)
            }
            R.id.mMagn -> {
                type = 4
                data = floatArrayOf(0.0f, 0.0f, 0.4f)
            }
        }
        drawer.setData(isHalf, type, data)
        binding.glSurfaceView.requestRender()
        return super.onOptionsItemSelected(item)
    }


    private fun onCall(data: ByteBuffer, bmpWidth: Int, bmpHeight: Int) {
        Thread {
            val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(data)
            data.clear()
            ImageUtils.save2Album(bitmap, "123.jpg", Bitmap.CompressFormat.JPEG)
            runOnUiThread {
//                binding.imageView.setImageBitmap(bitmap)
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            }
        }.start()

    }


}