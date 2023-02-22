package com.matt.ffmpegplay

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpegplay.databinding.ActivityNativeOpenglBinding
import com.matt.opengllib.render.MyGLRender
import com.matt.opengllib.render.MyGLSurfaceView
import com.matt.opengllib.render.NativeRender

class NativeOpenglActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNativeOpenglBinding
    private var myGLSurfaceView: MyGLSurfaceView? = null
    private val nativeRender by lazy { MyGLRender() }
    private val bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.a, BitmapFactory.Options().apply { inScaled = false })
    }
    private val layoutParams by lazy {
        RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ).apply { addRule(RelativeLayout.CENTER_IN_PARENT) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeOpenglBinding.inflate(layoutInflater)
        setContentView(binding.root)
        nativeRender.setDrawerType(NativeRender.SAMPLE_TYPE_KEY_TRIANGLE)
        myGLSurfaceView = MyGLSurfaceView(this, nativeRender)
        binding.rootView.addView(myGLSurfaceView, layoutParams)
    }


    override fun onDestroy() {
        super.onDestroy()
        nativeRender.destroy()
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
                nativeRender.setImageData(bitmap)
            }
            R.id.btn3 -> {
                nativeRender.setDrawerType(NativeRender.SAMPLE_TYPE_KEY_TRIANGLE)
            }
            R.id.btn4 -> {
                nativeRender.setDrawerType(NativeRender.SAMPLE_TYPE_KEY_TRIANGLE)
            }
        }
        myGLSurfaceView?.requestRender()
        return super.onOptionsItemSelected(item)
    }

}