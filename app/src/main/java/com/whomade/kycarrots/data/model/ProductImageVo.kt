package com.whomade.kycarrots.data.model

data class ProductImageVo @JvmOverloads constructor(
    val imageId: String? = null,
    val productId: String? = null,
    val imageCd: String?  = null,
    val imageUrl: String? = null,
    val imageName: String? = null,
    val represent: String, // 대표 여부 (0/1)
    val imageSize: Long? = null,
    val imageText: String? = null,
    val imageType: String? = null,
    val registerNo: String = "",
    val registDt: String? = null,
    val updusrNo: String = "",
    val updtDt: String? = null
)
