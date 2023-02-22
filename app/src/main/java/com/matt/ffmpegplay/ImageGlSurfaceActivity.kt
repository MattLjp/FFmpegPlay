package com.matt.ffmpegplay

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.matt.opengllib.drawer.ImageDrawer
import com.matt.opengllib.render.SimpleRender

/**
 * Created by Liaojp on 2023/2/7.
 */
class ImageGlSurfaceActivity : AppCompatActivity() {
    private lateinit var glView: GLSurfaceView
    private var render = SimpleRender()
    private val drawer by lazy { ImageDrawer(this, bitmap) }
    private val bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.a, BitmapFactory.Options().apply { inScaled = false })
    }

    private var isHalf = false
    private var type = 0
    private var data = floatArrayOf(0.0f, 0.0f, 0.0f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glView = GLSurfaceView(this)
        setContentView(glView)
        glView.setEGLContextClientVersion(3)
        render.addDrawer(drawer)
        glView.setRenderer(render)
        glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

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
        glView.requestRender()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        render.destroy()
    }
}