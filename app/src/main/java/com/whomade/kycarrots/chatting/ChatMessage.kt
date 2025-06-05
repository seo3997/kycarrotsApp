package com.whomade.kycarrots.chatting

data class ChatMessage(
    val message: String,
    val isMe: Boolean   // true: 내가 보낸 메시지, false: 상대
)
