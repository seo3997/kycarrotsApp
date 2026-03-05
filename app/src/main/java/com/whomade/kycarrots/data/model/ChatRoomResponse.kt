package com.whomade.kycarrots.data.model

data class ChatRoomResponse(
    val id: Long,
    val roomId: String,
    val buyerId: String,
    val branchId: String,
    val productId: String,
    val createdAt: String
)
