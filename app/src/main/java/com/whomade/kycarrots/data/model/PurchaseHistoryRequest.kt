package com.whomade.kycarrots.data.model

// com.whomade.kycarrots.data.model.purchase.PurchaseHistoryRequest.kt
data class PurchaseHistoryRequest(
    val productId: Long,
    val buyerNo: Long,
    val roomId: String? = null,   // 있으면 전달
    val sellerNo:Long
)
