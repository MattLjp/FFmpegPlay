package com.matt.ffmpegplay

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.matt.opengllib.drawer.IndicesCubeDrawer
import com.matt.opengllib.drawer.RectangleDrawer
import com.matt.opengllib.drawer.TextureDrawer
import com.matt.opengllib.drawer.TriangleDrawer
import com.matt.opengllib.render.SimpleRender

class SimpleGLSurfaceActivity : AppCompatActivity() {
    private lateinit var glView: GLSurfaceView
    private val render by lazy { SimpleRender() }
    private var drawer = TriangleDrawer()
    private val bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.a, BitmapFactory.Options().apply { inScaled = false })
    }

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
        menuInflater.inflate(R.menu.menu_simple, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn1 -> {
                render.setDrawer(TriangleDrawer())
            }
            R.id.btn2 -> {
                render.setDrawer(RectangleDrawer())
            }
            R.id.btn3 -> {
                render.setDrawer(IndicesCubeDrawer())
            }
            R.id.btn4 -> {
                render.setDrawer(TextureDrawer(this, bitmap))
            }
        }
        glView.requestRender()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        render.destroy()
    }
}