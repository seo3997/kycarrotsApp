package com.whomade.kycarrots.chatting

data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val roomId: String = "",
    val type: String = "text",
    val time: String = "",         // 전송 시간 추가
    val senderGroup: String? = null,
    val receiveGroup: String? = null,
    var isMe: Boolean = false
)