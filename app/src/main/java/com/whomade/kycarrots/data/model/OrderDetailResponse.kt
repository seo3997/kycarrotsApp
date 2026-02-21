package com.whomade.kycarrots.data.model

import com.google.gson.annotations.SerializedName

data class OrderDetailResponse(
    @SerializedName("items") val items: List<OrderDetailItem>,
    @SerializedName("order") val order: OrderInfo
)

data class OrderDetailItem(
    @SerializedName("orderItemId") val orderItemId: Long,
    @SerializedName("orderId") val orderId: Long,
    @SerializedName("productId") val productId: Long,
    @SerializedName("productName") val productName: String,
    @SerializedName("optionName") val optionName: String?,
    @SerializedName("unitPrice") val unitPrice: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("registerNo") val registerNo: Long,
    @SerializedName("registDt") val registDt: String,
    @SerializedName("updusrNo") val updusrNo: Long,
    @SerializedName("updtDt") val updtDt: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("categoryGroup") val categoryGroup: String?,
    @SerializedName("categoryMid") val categoryMid: String?,
    @SerializedName("categoryScls") val categoryScls: String?
)

data class OrderInfo(
    @SerializedName("orderId") val orderId: Long,
    @SerializedName("orderNo") val orderNo: String,
    @SerializedName("userNo") val userNo: Long,
    @SerializedName("orderStatus") val orderStatus: String,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("totalItemAmount") val totalItemAmount: Int,
    @SerializedName("deliveryFee") val deliveryFee: Int,
    @SerializedName("discountAmount") val discountAmount: Int,
    @SerializedName("totalPayAmount") val totalPayAmount: Int,
    @SerializedName("receiverName") val receiverName: String,
    @SerializedName("receiverPhone") val receiverPhone: String,
    @SerializedName("zipCode") val zipCode: String,
    @SerializedName("address1") val address1: String,
    @SerializedName("address2") val address2: String?,
    @SerializedName("orderMemo") val orderMemo: String?,
    @SerializedName("orderedAt") val orderedAt: String,
    @SerializedName("paidAt") val paidAt: String?,
    @SerializedName("cancelledAt") val cancelledAt: String?,
    @SerializedName("registerNo") val registerNo: Long,
    @SerializedName("registDt") val registDt: String,
    @SerializedName("updusrNo") val updusrNo: Long,
    @SerializedName("updtDt") val updtDt: String
)
