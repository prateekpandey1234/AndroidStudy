package com.prateek.stockTradingApp.data.remote

import com.prateek.androidstudy.data.remote.websocket.ConnectionState
import com.prateek.stockTradingApp.module.StockQualifier
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject


class StockWSSService @Inject constructor(

    @StockQualifier val httpClint: OkHttpClient
) : StockWSSInterface{


    private var webSocket : WebSocket?=null


    private var _messages = MutableSharedFlow<String>(
        replay = 50,
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)

    override fun connect(url: String) {
        _connectionState.value = ConnectionState.CONNECTED

    }

    override fun disconnect() {
    }

    override fun observeConnectionState(): Flow<ConnectionState> = _connectionState.asStateFlow()


    override fun observeMessages(): Flow<String>  = _messages.asSharedFlow()


    private val websocketListener = object : WebSocketListener(){

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
        }
    }

}
