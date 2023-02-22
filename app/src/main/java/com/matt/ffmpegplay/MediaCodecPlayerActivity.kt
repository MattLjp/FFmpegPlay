package com.matt.ffmpegplay

import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.matt.ffmpeglib.media.ANativePlayer
import com.matt.ffmpeglib.media.decoder.BaseDecoder
import com.matt.ffmpeglib.media.decoder.Frame
import com.matt.ffmpeglib.media.decoder.IDecoderStateListener
import com.matt.ffmpegplay.databinding.ActivityPlayerBinding

/**
 * Created by Liaojp on 2023/1/11.
 */
class MediaCodecPlayerActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private val videoPath by lazy { externalCacheDir.toString() + "/matt/one_piece.mp4" }

    private lateinit var binding: ActivityPlayerBinding
    private val aNativePlayer by lazy { ANativePlayer() }
    private var isTouch = false
    private var isPlay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        aNativePlayer.setStateListener(object : IDecoderStateListener {
            override fun decoderError(decodeJob: BaseDecoder?, msg: String) {}

            override fun decoderReady(decodeJob: BaseDecoder?, duration: Long) {
                runOnUiThread {
                    binding.seekBar.max = duration.toInt()
                }
            }

            override fun decoderPlay(decodeJob: BaseDecoder?) {
                runOnUiThread {
                    isPlay = true
                    binding.play.text = "暂停"
                }
            }

            override fun decoderPause(decodeJob: BaseDecoder?) {
                runOnUiThread {
                    isPlay = false
                    binding.play.text = "播放"
                }
            }

            override fun decodeOneFrame(decodeJob: BaseDecoder?, frame: Frame, time: Long) {
                runOnUiThread {
                    if (!isTouch) {
                        binding.seekBar.progress = time.toInt()
                    }
                }
            }

            override fun decoderFinish(decodeJob: BaseDecoder?) {
                runOnUiThread {
                    isPlay = false
                    binding.play.text = "播放"
                }
            }

            override fun decoderStop(decodeJob: BaseDecoder?) {}

        })

        binding.play.setOnClickListener {
            if (isPlay) {
                aNativePlayer.pause()
            } else {
                aNativePlayer.play()
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
                aNativePlayer.seekToPosition(seekBar.progress)
            }
        })
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        aNativePlayer.createPlay(videoPath, holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        aNativePlayer.stop()
    }
}