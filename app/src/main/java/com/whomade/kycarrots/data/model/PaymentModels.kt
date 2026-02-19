package com.whomade.kycarrots.data.model

data class OrderCreateRequest(
    val userNo: Long,
    val totalItemAmount: Int,
    val deliveryFee: Int,
    val discountAmount: Int,
    val totalPayAmount: Int,
    val receiverName: String,
    val receiverPhone: String,
    val zipCode: String,
    val address1: String,
    val address2: String,
    val orderMemo: String,
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int,
    val optionName: String?
)

data class OrderCreateResponse(
    val success: Boolean,
    val orderId: Long,
    val orderNo: String,
    val message: String? = null
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
