package com.matt.ffmpegplay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpegplay.databinding.ActivityObj3dBinding

class Obj3DActivity : AppCompatActivity() {
    private lateinit var binding: ActivityObj3dBinding
    private val obj3DPath = "eva.obj"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObj3dBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.obj3DView.setObjPath(obj3DPath)
    }

    override fun onResume() {
        super.onResume()
        binding.obj3DView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.obj3DView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.obj3DView.destroy()
    }

}