package com.whomade.kycarrots.data.model

data class ProductItem(
    val productId: String,
    val saleStatus: String,
    val updusrNo: Int,
    val rejectReason: String? = null, // 반려사유 (nullable)
    val systemType: String
)
