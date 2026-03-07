package com.whomade.kycarrots.data.model

import com.google.gson.annotations.SerializedName

data class AdItem(
    @SerializedName(value = "productId", alternate = ["PRODUCT_ID"])
    val productId: String,
    @SerializedName(value = "title", alternate = ["TITLE"])
    val title: String,
    @SerializedName(value = "description", alternate = ["DESCRIPTION"])
    val description: String,
    @SerializedName(value = "price", alternate = ["PRICE"])
    val price: String,
    @SerializedName(value = "imageUrl", alternate = ["IMAGE_URL"])
    val imageUrl: String,
    @SerializedName(value = "userId", alternate = ["USER_NO"])
    val userId: String,
    @SerializedName(value = "orderNo", alternate = ["ORDER_NO"])
    val orderNo: String? = null,
    @SerializedName(value = "orderId", alternate = ["ORDER_ID"])
    val orderId: String? = null,
    @SerializedName(value = "paymentStatus", alternate = ["PAYMENT_STATUS", "ORDER_STATUS"])
    val paymentStatus: String? = null,
    @SerializedName(value = "orderStatusNm", alternate = ["ORDER_STATUS_NM"])
    val orderStatusNm: String? = null,
    @SerializedName(value = "deliveredAt", alternate = ["DELIVERED_AT"])
    val deliveredAt: String? = null,
    @SerializedName(value = "saleStatusNm", alternate = ["SALE_STATUS_NM"])
    val saleStatusNm: String? = null,
    @SerializedName(value = "deliveryCompanyNm", alternate = ["DELIVERY_COMPANY_NM"])
    val deliveryCompanyNm: String? = null,
    @SerializedName(value = "trackingNo", alternate = ["TRACKING_NO"])
    val trackingNo: String? = null,
    @SerializedName(value = "orderedAt", alternate = ["ORDERED_AT"])
    val orderedAt: String? = null
)