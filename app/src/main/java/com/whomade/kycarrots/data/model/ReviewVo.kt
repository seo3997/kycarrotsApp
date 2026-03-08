package com.whomade.kycarrots.data.model

data class ReviewVo(
    val reviewId: String? = null,
    val productId: String? = null,
    val userNo: String? = null,
    val userNm: String? = null,
    val rating: Int = 0,
    val contents: String? = null,
    val registDt: String? = null,
    val displayYn: String? = "Y",
    val atchDocId: String? = null,
    val fileRltvPath: String? = null
)
