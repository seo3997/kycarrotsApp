package com.whomade.kycarrots.chatting

data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val roomId: String = "",
    val type: String = "text",
    var isMe: Boolean = false
)