package com.whomade.kycarrots.data.model

import com.google.gson.annotations.SerializedName

// com.whomade.kycarrots.data.model.chat.ChatBuyerDto.kt
data class ChatBuyerDto(
    @SerializedName("room_id")    val roomId: String,
    @SerializedName("product_id") val productId: Long,
    @SerializedName("seller_id")  val sellerId: String,
    @SerializedName("buyer_id")   val buyerId: String,
    @SerializedName("buyer_no")   val buyerNo: Long,
    @SerializedName("buyer_nm")   val buyerNm: String,
    @SerializedName("seller_no")  val sellerNo: Long,
    @SerializedName("seller_nm")  val sellerNm: String
)