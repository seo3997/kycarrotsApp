package com.whomade.kycarrots.data.model

data class BranchInfoVo(
    val branchId: Long = 0,
    val branchCode: String? = null,
    val branchName: String? = null,
    val logoImageUrl: String? = null,
    val branchStatus: String? = null,
    val tossClientKey: String? = null,
    val bankCd: String? = null,
    val accountNo: String? = null,
    val accountHolder: String? = null,
    val baseShippingFee: Int = 0,
    val freeShippingThreshold: Int = 0,
    val extraShippingFee: Int = 0,
    val isUseCustomPrice: Int = 0,
    val companyName: String? = null,
    val representativeName: String? = null,
    val businessNumber: String? = null,
    val tongsinNumber: String? = null,
    val csPhone: String? = null,
    val address: String? = null
)
