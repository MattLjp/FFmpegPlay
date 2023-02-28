package com.matt.ffmpegplay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpeglib.etc.EtcDrawer
import com.matt.ffmpegplay.databinding.ActivityEtcAniBinding

class EtcAniActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEtcAniBinding
    private val nowMenu = "assets/etczip/cc.zip"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEtcAniBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etcAniView.setOnClickListener {
            if (!binding.etcAniView.isPlay()) {
                binding.etcAniView.setAnimation(nowMenu, 50)
                binding.etcAniView.start()
            }
        }
        binding.etcAniView.setStateChangeListener {
            if (it == EtcDrawer.STOP) {
                if (!binding.etcAniView.isPlay()) {
                    binding.etcAniView.setAnimation(nowMenu, 50)
                    binding.etcAniView.start()
                }
            }
        }
    }
}