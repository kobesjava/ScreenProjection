package com.jaki.sp.send

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import java.nio.ByteBuffer

class SendMediaCodecH265(
    private val mediaProjection: MediaProjection,
    private val listener: SendMediaCodecListener
) : Runnable {

    var mediaCodec: MediaCodec? = null
    var display: VirtualDisplay? = null
    private val nalI = 19
    private val nalVps = 32
    var vpsSpsPpsBuf: ByteArray? = null

    fun init() {
        try {
            val mediaFormat =
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, 1080, 2280)
            mediaFormat.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1080 * 2280)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)//1秒一个I帧
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            mediaCodec?.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = mediaCodec?.createInputSurface()
            display = mediaProjection.createVirtualDisplay(
                "display", 1080, 2280,
                1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun run() {
        mediaCodec?.let {
            it.start()
            val bufferInfo = MediaCodec.BufferInfo()
            while (true) {
                val outPutIndex = it.dequeueOutputBuffer(bufferInfo, 10000)
                if (outPutIndex >= 0) {
                    it.getOutputBuffer(outPutIndex)?.let {
                        dealFrame(it, bufferInfo)
                    }
                    it.releaseOutputBuffer(outPutIndex, false)
                }
            }
        }
    }

    private fun dealFrame(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        var offset = 4 //默认分隔符是 00 00 00 01
        if (byteBuffer.get(2).toInt() == 0x01) { //分隔符是 00 00 01
            offset = 3
        }
        val type = (byteBuffer.get(offset).toInt() and 0x7E) shr 1
        if (type == nalVps) {
            vpsSpsPpsBuf = ByteArray(bufferInfo.size)
            byteBuffer.get(vpsSpsPpsBuf)
        } else if (type == nalI) {
            val bytes = ByteArray(bufferInfo.size)
            byteBuffer.get(bytes)
            val newBUf = ByteArray((vpsSpsPpsBuf?.size ?: 0) + bytes.size)
            System.arraycopy(vpsSpsPpsBuf, 0, newBUf, 0, vpsSpsPpsBuf?.size ?: 0)
            System.arraycopy(bytes, 0, newBUf, vpsSpsPpsBuf?.size ?: 0, bytes.size)
            listener.onMediaCodec(newBUf)
        } else {
            val bytes = ByteArray(bufferInfo.size)
            byteBuffer.get(bytes)
            listener.onMediaCodec(bytes)
        }
    }

    fun stop() {
        mediaCodec?.stop()
        display?.release()
        mediaProjection.stop()
    }

    interface SendMediaCodecListener {
        fun onMediaCodec(bytes: ByteArray)
    }

}