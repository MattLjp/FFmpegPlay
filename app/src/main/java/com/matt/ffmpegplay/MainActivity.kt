package com.matt.ffmpegplay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpeglib.FFmpegPlay
import com.matt.ffmpegplay.databinding.ActivityMainBinding
import com.matt.ffmpegplay.utils.CommonUtils
import com.matt.ffmpegplay.utils.PermissionsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var permissionsHelper: PermissionsHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionsHelper = PermissionsHelper()
        permissionsHelper?.initPermissions(this)
        permissionsHelper?.requestPermissions(this) {
            CommonUtils.copyAssetsDirToSDCard(this, "matt", externalCacheDir.toString())
        }
        // Example of a call to a native method
        binding.sampleText.text = FFmpegPlay.getFFmpegVersion()

        binding.button1.setOnClickListener {
            startActivity(Intent(this, MediaCodecPlayerActivity::class.java))
        }
        binding.button2.setOnClickListener {
            startActivity(Intent(this, FFmpegPlayerActivity::class.java))
        }
        binding.button3.setOnClickListener {
            startActivity(Intent(this, OpenglActivity::class.java))
        }
    }

}