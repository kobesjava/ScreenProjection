package com.jaki.sp.recive

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import java.lang.Exception

class ReciveMdeiaCodecH264(surface: Surface) : ReciveMediaCodec {

    private var mediaCodec: MediaCodec? = null

    init {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1080, 2280)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 1080 * 2280)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec?.configure(format, surface, null, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun start() {
        mediaCodec?.start()
    }

    override fun stop() {
        mediaCodec?.stop()
    }

    override fun onReciveData(data: ByteArray) {
        mediaCodec?.let {
            val inputIndex = it.dequeueInputBuffer(10000)
            if (inputIndex >= 0) {
                val inputBuffer = it.getInputBuffer(inputIndex)
                inputBuffer?.let {
                    it.clear()
                    it.put(data, 0, data.size)
                    mediaCodec?.queueInputBuffer(
                        inputIndex,
                        0,
                        data.size,
                        System.currentTimeMillis(),
                        0
                    )
                }
            }
            val bufferInfo = MediaCodec.BufferInfo()
            var outPutIndex = it.dequeueOutputBuffer(bufferInfo, 10000)
            while (outPutIndex >= 0) {
                it.releaseOutputBuffer(outPutIndex, true)
                outPutIndex = it.dequeueOutputBuffer(bufferInfo, 10000)
            }
        }
    }

}