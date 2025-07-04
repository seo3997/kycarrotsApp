package com.whomade.kycarrots.data.model

data class ProductVo @JvmOverloads constructor(
    val productId: String? = null,
    val userNo: String,
    val title: String,
    val description: String,
    val price: String,
    val categoryGroup: String,
    val categoryMid: String,
    val categoryScls: String,
    val saleStatus: String,
    val areaGroup: String,
    val areaMid: String,
    val areaScls: String,
    val quantity: String,
    val unitGroup: String,
    val unitCode: String,
    val desiredShippingDate: String,
    val registerNo: String,
    val registDt: String? = null,
    val updusrNo: String,
    val updtDt: String? = null,
    val imageUrl: String? = null,
    // 추가된 이름(NM) 필드
    val categoryMidNm: String = "",
    val categorySclsNm: String = "",
    val areaMidNm: String = "",
    val areaSclsNm: String = "",
    val unitCodeNm: String = "",
    val saleStatusNm: String = "",
    val userId: String = ""
)
