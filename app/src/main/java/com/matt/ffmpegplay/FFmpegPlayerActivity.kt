package com.matt.ffmpegplay

import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpeglib.FFmpegPlay
import com.matt.ffmpeglib.FFmpegPlay.Companion.MSG_DECODER_FINISH
import com.matt.ffmpeglib.FFmpegPlay.Companion.MSG_DECODER_INIT_ERROR
import com.matt.ffmpeglib.FFmpegPlay.Companion.MSG_DECODER_PAUSE
import com.matt.ffmpeglib.FFmpegPlay.Companion.MSG_DECODER_PLAY
import com.matt.ffmpeglib.FFmpegPlay.Companion.MSG_DECODER_PREPARE
import com.matt.ffmpeglib.FFmpegPlay.Companion.MSG_DECODER_STOP
import com.matt.ffmpeglib.FFmpegPlay.Companion.MSG_DECODER_TIME
import com.matt.ffmpegplay.databinding.ActivityPlayerBinding

/**
 * Created by Liaojp on 2023/1/8.
 */
class FFmpegPlayerActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private val videoPath = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4"
    //    private val videoPath = "http://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4"

    private lateinit var binding: ActivityPlayerBinding
    private val ffmpegPlay by lazy { FFmpegPlay() }
    private var isTouch = false
    private var isPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ffmpegPlay.addEventCallback(object : FFmpegPlay.EventCallback {
            override fun onPlayerEvent(msgType: Int, msgValue: Int) {
                runOnUiThread {
                    when (msgType) {
                        MSG_DECODER_INIT_ERROR -> {}
                        MSG_DECODER_PREPARE -> {
                            binding.seekBar.max = msgValue
                        }
                        MSG_DECODER_PLAY -> {
                            isPlay = true
                            binding.play.text = "暂停"
                        }
                        MSG_DECODER_PAUSE -> {
                            isPlay = false
                            binding.play.text = "播放"
                        }
                        MSG_DECODER_FINISH -> {
                            isPlay = false
                            binding.play.text = "播放"
                        }
                        MSG_DECODER_STOP -> {}
                        MSG_DECODER_TIME -> {
                            if (!isTouch) {
                                binding.seekBar.progress = msgValue
                            }
                        }
                    }
                }
            }

        })

        binding.play.setOnClickListener {
            if (isPlay) {
                ffmpegPlay.pause()
            } else {
                ffmpegPlay.play()
            }
        }


        binding.surfaceView.holder.addCallback(this)

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isTouch = false
                ffmpegPlay.seekToPosition(seekBar.progress)
            }
        })
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        ffmpegPlay.createPlay(videoPath, holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        ffmpegPlay.stop()
    }
}