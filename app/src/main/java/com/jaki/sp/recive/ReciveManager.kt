package com.jaki.sp.recive

import android.view.Surface
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class ReciveManager(surface: Surface) : DefaultLifecycleObserver,
    ReciveSocketLive.ReciveSocketCallback {

    private val screenLive = ReciveSocketLive(this)
    private val mediaCodec = ReciveMdeiaCodecH265(surface)

    fun start() {
        screenLive.start()
        mediaCodec.start()
    }

    override fun onCallBack(data: ByteArray) {
        mediaCodec.onReciveData(data)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        screenLive.stop()
        mediaCodec.stop()
    }
}