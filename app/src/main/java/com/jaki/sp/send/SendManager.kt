package com.jaki.sp.send

import android.media.projection.MediaProjection
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

class SendManager(mediaProjection: MediaProjection) :
    SendMediaCodecH265.SendMediaCodecListener,
    DefaultLifecycleObserver {

    private val threadPool = Executors.newSingleThreadExecutor()
    private val socketLive = SendSocketLive()
    private val mediaCodec = SendMediaCodecH265(mediaProjection, this)

    fun start() {
        mediaCodec.init()
        threadPool.submit(mediaCodec)
        socketLive.start()
    }

    override fun onMediaCodec(bytes: ByteArray) {
        socketLive.send(bytes)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        socketLive.stop()
        mediaCodec.stop()
        threadPool.shutdownNow()
    }
}