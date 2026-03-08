package com.whomade.kycarrots.data.model

data class QnaVo(
    val qnaId: String? = null,
    val productId: String? = null,
    val userNo: String? = null,
    val userNm: String? = null,
    val title: String? = null,
    val contents: String? = null,
    val secretYn: String? = "N",
    val registDt: String? = null,
    val qnaStatus: String? = "10",
    val answerContents: String? = null,
    val answererNm: String? = null,
    val answeredAt: String? = null
)
