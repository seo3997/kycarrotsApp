package com.whomade.kycarrots.data.model

// package com.whomade.kycarrots.data.model
data class AdListRequest(
    val token: String,
    val adCode: Int,
    val pageno: Int,
    val categoryGroup: String? = "R010610",
    val categoryMid: String? = null,
    val categoryScls: String? = null,
    val areaGroup: String? = "R010070",
    val areaMid: String? = null,
    val areaScls: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    var saleStatus: String? = "1",
    var memberCode: String? = ""
)
