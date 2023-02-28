package com.matt.ffmpegplay

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpeglib.drawer.IndicesCubeDrawer
import com.matt.ffmpeglib.drawer.RectangleDrawer
import com.matt.ffmpeglib.drawer.TextureDrawer
import com.matt.ffmpeglib.drawer.TriangleDrawer
import com.matt.ffmpeglib.egl.CustomerGLRenderer
import com.matt.ffmpegplay.databinding.ActivityEglplayerBinding

class EGLPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEglplayerBinding
    private var render = CustomerGLRenderer()
    private var drawer = TriangleDrawer()
    private val bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.a, BitmapFactory.Options().apply { inScaled = false })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEglplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        render.addDrawer(drawer)
        render.setRenderMode(CustomerGLRenderer.RenderMode.RENDER_CONTINUOUSLY)
        render.setSurface(binding.sfv)
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
        return super.onOptionsItemSelected(item)
    }

}