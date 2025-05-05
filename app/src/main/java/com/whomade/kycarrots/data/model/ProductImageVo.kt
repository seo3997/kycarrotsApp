package com.whomade.kycarrots.data.model

data class ProductImageVo(
    val imageId: Long? = null,
    val productId: Long? = null,
    val imageCd: String,
    val imageUrl: String? = null,
    val imageName: String? = null,
    val represent: Int = 0, // 대표 여부 (0/1)
    val imageSize: Long? = null,
    val imageText: String? = null,
    val imageType: String? = null,
    val registerNo: Int,
    val registDt: String? = null,
    val updusrNo: Int,
    val updtDt: String? = null
)
