package com.matt.ffmpegplay

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ImageUtils
import com.matt.ffmpegplay.databinding.ActivityCameraGlSurfaceBinding
import com.matt.ffmpeglib.drawer.CameraDrawer
import com.matt.ffmpeglib.render.SimpleRender
import java.nio.ByteBuffer

/**
 * Created by Liaojp on 2023/2/7.
 */
class CameraGlSurfaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraGlSurfaceBinding

    private var render = SimpleRender()
    private val drawer by lazy { CameraDrawer(this, binding.glSurfaceView) }

    private var type = 0
    private var data = floatArrayOf(0.0f, 0.0f, 0.0f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraGlSurfaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawer.callback = { data, width, height ->
            onCall(data, width, height)
        }
        binding.glSurfaceView.setEGLContextClientVersion(3)
        render.addDrawer(drawer)
        binding.glSurfaceView.setRenderer(render)
        binding.ibShutter.setOnClickListener {
            drawer.takePhoto()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        render.destroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_camera, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
        }
        drawer.setData(type, data)
        binding.glSurfaceView.requestRender()
        return super.onOptionsItemSelected(item)
    }

    private fun onCall(data: ByteBuffer, bmpWidth: Int, bmpHeight: Int) {
        Thread {
            val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(data)
            ImageUtils.save2Album(bitmap, "456.jpg", Bitmap.CompressFormat.JPEG)
            data.clear()
            runOnUiThread {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}