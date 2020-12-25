package com.jaki.sp.send

import android.util.Log
import com.jaki.sp.Config
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class SendSocketLive {

    var webSockets = mutableListOf<WebSocket>()

    private val webSocketServer = object : WebSocketServer(InetSocketAddress(Config.port)) {
        override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
            conn?.let {
                webSockets.add(conn)
            }
        }

        override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
            webSockets.remove(conn)
            Log.i("Jaki", "SendSocketLive onClose onClose code=$code  reason=$reason")
        }

        override fun onMessage(conn: WebSocket?, message: String?) {
        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            Log.i("Jaki", "SendSocketLive onError onError Exception=$ex")
        }

        override fun onStart() {
            Log.i("Jaki", "onStart onStart")
        }
    }

    fun start() {
        webSocketServer.start()
    }

    fun send(byteArr: ByteArray) {
        Log.i("Jaki", "SendSocketLive 发送消息 ${byteArr.size}")
        webSockets.forEach {
            if (it.isOpen) {
                it.send(byteArr)
            }
        }
    }

    fun stop() {
        webSockets.forEach {
            it.close()
        }
        webSockets.clear()
        webSocketServer.stop()
    }

}