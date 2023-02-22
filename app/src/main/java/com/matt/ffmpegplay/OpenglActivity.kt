package com.matt.ffmpegplay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpegplay.databinding.ActivityOpenglBinding

class OpenglActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpenglBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenglBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button10.setOnClickListener {
            startActivity(Intent(this, SimpleGLSurfaceActivity::class.java))
        }
        binding.button11.setOnClickListener {
            startActivity(Intent(this, ImageGlSurfaceActivity::class.java))
        }
        binding.button12.setOnClickListener {
            startActivity(Intent(this, CameraGlSurfaceActivity::class.java))
        }
        binding.button13.setOnClickListener {
            startActivity(Intent(this, FBOActivity::class.java))
        }
        binding.button14.setOnClickListener {
            startActivity(Intent(this, EGLPlayerActivity::class.java))
        }
        binding.button15.setOnClickListener {
            startActivity(Intent(this, EtcAniActivity::class.java))
        }
        binding.button16.setOnClickListener {
            startActivity(Intent(this, Obj3DActivity::class.java))
        }
        binding.button17.setOnClickListener {
            startActivity(Intent(this, NativeOpenglActivity::class.java))
        }
    }
}