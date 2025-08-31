package com.prateek.androidstudy.viewmodel.websocket

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prateek.androidstudy.data.remote.websocket.ConnectionState
import com.prateek.androidstudy.data.remote.websocket.WebSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import javax.inject.Inject


@HiltViewModel
class WebSocketViewModel @Inject constructor(private val webSocketService: WebSocketService) : ViewModel(){
    private val _messages = MutableLiveData<List<String>>()
    val messages: LiveData<List<String>> = _messages

    private val _connectionState = MutableLiveData<ConnectionState>()
    val connectionState: LiveData<ConnectionState> = _connectionState

    init {
        connect()
        viewModelScope.launch {
            webSocketService.observeMessages()
                .onEach { message ->
                    Log.d("ViewModel", "Flow emitted: $message")
                }
                .collect { message ->
                    Log.d("ViewModel", "Collector received: $message")
                    val currentMessages = _messages.value.orEmpty().toMutableList()
                    currentMessages.add(message)
                    _messages.postValue(currentMessages)
                }

        }
    }

    fun connect() = viewModelScope.launch(Dispatchers.IO){
        webSocketService.connect("wss://free.blr2.piesocket.com/v3/1?api_key=L9zgjnHSeXYpQ1aZyc664rR8wsRC3cwlHy9NzjiQ&notify_self=1")
        webSocketService.observeConnectionState().collect { state ->
            _connectionState.postValue(state)
        }

    }

    fun disconnect() = viewModelScope.launch(Dispatchers.IO){
        webSocketService.disconnect()
    }

    fun sendMessage(message:String) = viewModelScope.launch(Dispatchers.IO){
        webSocketService.sendMessage(message)
    }

}
