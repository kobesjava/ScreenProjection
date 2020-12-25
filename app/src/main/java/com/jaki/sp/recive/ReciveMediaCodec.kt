package com.jaki.sp.recive

interface ReciveMediaCodec {

    fun start()

    fun stop()

    fun onReciveData(data: ByteArray)

}