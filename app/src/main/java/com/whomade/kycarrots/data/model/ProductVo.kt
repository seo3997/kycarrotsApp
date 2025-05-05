package com.whomade.kycarrots.data.model

data class ProductVo(
    val productId: String? = null,
    val userNo: String,
    val title: String,
    val description: String,
    val price: String,
    val categoryGroup: String,
    val categoryMid: String,
    val categoryScls: String,
    val saleStatus: String,
    val registerNo: String,
    val registDt: String? = null,
    val updusrNo: String,
    val updtDt: String? = null,
    val imageUrl: String? = null
)
