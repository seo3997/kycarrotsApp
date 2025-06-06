package com.whomade.kycarrots.data.model

data class ChatMessageResponse(
    val id: Long,
    val roomId: String,
    val senderId: String,
    val message: String,
    val createdAt: String,   // ISO-8601 포맷 또는 서버 날짜 포맷에 맞게
    val isRead: Boolean
)
