package com.prateek.chatAppSystemDesign.other

class NavigationEvent {
    companion object{
        const val  HOME = "home"
        const val  CHANNEL = "channel"
        const val  ATTACHMENT_IMAGE = "image"
        const val  ATTACHMENT_VIDEO = "video"
        const val  MESSAGE_VIEWS = "message"
        const val  MESSAGE_REPLIES = "replies"
    }
}
sealed class NavigationEvents{
    data class NavigateTo(val route:String):NavigationEvents()
    object NavigateBack:NavigationEvents()
}