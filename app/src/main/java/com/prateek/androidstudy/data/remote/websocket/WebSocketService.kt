package com.prateek.androidstudy.data.remote.websocket

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient
) : WebSocketInterface {

    private var webSocket : WebSocket?=null
    private var _messages = MutableSharedFlow<String>(
        replay = 50,// Show last 50 messages to new screens
        extraBufferCapacity = 100,// buffer ccapcity
        onBufferOverflow = BufferOverflow.DROP_OLDEST //if buffer is filled then remove old
    )
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)

    override fun connect(url: String) {
        val req = Request.Builder().url(url).build()
        Log.d("TAG", "connect: $url")
        webSocket = okHttpClient.newWebSocket(req, webSocketListener)

        _connectionState.value = ConnectionState.CONNECTING


    }

    override fun disconnect() {
        webSocket?.close(1000,"Close socket")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED

    }

    override fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    override fun observeMessages(): Flow<String>  = _messages.asSharedFlow()

    override fun observeConnectionState(): Flow<ConnectionState> = _connectionState.asStateFlow()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
            _connectionState.value = ConnectionState.CONNECTED
            Log.d("TAG open", response.toString())

        }

        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            val bool = _messages.tryEmit(text) // emitting data out for collectors
            Log.d("TAG message", bool.toString()+" "+text.toString())

        }

        override fun onMessage(webSocket: okhttp3.WebSocket, bytes: ByteString) {
            _messages.tryEmit(bytes.utf8()) // emitting data out for collectors
            Log.d("TAG message 2", bytes.utf8())

        }

        //indicating one connection is stopping
        override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
            _connectionState.value = ConnectionState.DISCONNECTED
            Log.d("TAG closing", reason+" "+code.toString())

        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionState.value = ConnectionState.DISCONNECTED
            Log.d("TAG closed", reason+" "+code.toString())

        }

        override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response?) {
            _connectionState.value = ConnectionState.FAILED
            Log.d("TAG fail", t.toString())

        }


    }

}


interface WebSocketInterface {
    fun connect(url: String)
    fun disconnect()
    fun sendMessage(message: String)
    fun observeMessages(): Flow<String>
    fun observeConnectionState(): Flow<ConnectionState>
}

enum class ConnectionState {
    CONNECTING, CONNECTED, DISCONNECTED, FAILED
}