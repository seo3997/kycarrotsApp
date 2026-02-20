package com.whomade.kycarrots.data.model

import com.google.gson.annotations.SerializedName

data class OrderCreateRequest(
    @SerializedName("userNo") val userNo: Long,
    @SerializedName("totalItemAmount") val totalItemAmount: Int,
    @SerializedName("deliveryFee") val deliveryFee: Int,
    @SerializedName("discountAmount") val discountAmount: Int,
    @SerializedName("totalPayAmount") val totalPayAmount: Int,
    @SerializedName("receiverName") val receiverName: String,
    @SerializedName("receiverPhone") val receiverPhone: String,
    @SerializedName("zipCode") val zipCode: String,
    @SerializedName("address1") val address1: String,
    @SerializedName("address2") val address2: String,
    @SerializedName("orderMemo") val orderMemo: String,
    @SerializedName("items") val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    @SerializedName("productId") val productId: Long,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("optionName") val optionName: String?
)

data class OrderCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("orderId") val orderId: Long,
    @SerializedName("orderNo") val orderNo: String,
    @SerializedName("message") val message: String? = null
)

data class PaymentConfirmRequest(
    val paymentKey: String,
    val orderId: String, // This is orderNo in server's DataMap usually, or orderId? payment.md says orderId but value is ORDER-...
    val amount: Int
)

data class PaymentConfirmResponse(
    val success: Boolean,
    val message: String?
)
