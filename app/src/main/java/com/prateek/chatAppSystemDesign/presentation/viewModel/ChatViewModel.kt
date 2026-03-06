package com.prateek.chatAppSystemDesign.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.prateek.chatAppSystemDesign.other.NavigationEvent
import com.prateek.chatAppSystemDesign.other.NavigationEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.*


class ChatViewModel : ViewModel() {
    private val _navigationEvent =
        Channel<NavigationEvents>(Channel.BUFFERED)// allowing only 64 events at a single time
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun navigateToScreen(route: String) {
        _navigationEvent.trySend(NavigationEvents.NavigateTo(route))
    }




}