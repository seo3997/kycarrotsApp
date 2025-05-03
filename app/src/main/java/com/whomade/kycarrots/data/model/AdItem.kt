package com.whomade.kycarrots.data.model

data class AdItem(
    val ad_idx: Int,
    val ad_nm: String,
    val ad_mainurl: String,
    val ad_point: Int,
    val ad_content: String,
    val ad_etc: String,
    val ad_brief: String,
    val ad_rating: Float,
    val ad_event: Int
)