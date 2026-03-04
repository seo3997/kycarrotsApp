package com.whomade.kycarrots.data.model

import com.google.gson.annotations.SerializedName

data class BranchInfoVo(
    @SerializedName(value = "BRANCH_ID", alternate = ["branchId"])
    val branchId: Long = 0,
    @SerializedName(value = "BRANCH_CODE", alternate = ["branchCode"])
    val branchCode: String? = null,
    @SerializedName(value = "BRANCH_NAME", alternate = ["branchName"])
    val branchName: String? = null,
    @SerializedName(value = "LOGO_IMAGE_URL", alternate = ["logoImageUrl"])
    val logoImageUrl: String? = null,
    @SerializedName(value = "BRANCH_STATUS", alternate = ["branchStatus"])
    val branchStatus: String? = null,
    @SerializedName(value = "TOSS_CLIENT_KEY", alternate = ["tossClientKey"])
    val tossClientKey: String? = null,
    @SerializedName(value = "BANK_CD", alternate = ["bankCd"])
    val bankCd: String? = null,
    @SerializedName(value = "ACCOUNT_NO", alternate = ["accountNo"])
    val accountNo: String? = null,
    @SerializedName(value = "ACCOUNT_HOLDER", alternate = ["accountHolder"])
    val accountHolder: String? = null,
    @SerializedName(value = "BASE_SHIPPING_FEE", alternate = ["baseShippingFee"])
    val baseShippingFee: Int = 0,
    @SerializedName(value = "FREE_SHIPPING_THRESHOLD", alternate = ["freeShippingThreshold"])
    val freeShippingThreshold: Int = 0,
    @SerializedName(value = "EXTRA_SHIPPING_FEE", alternate = ["extraShippingFee"])
    val extraShippingFee: Int = 0,
    @SerializedName(value = "IS_USE_CUSTOM_PRICE", alternate = ["isUseCustomPrice"])
    val isUseCustomPrice: Int = 0,
    @SerializedName(value = "COMPANY_NAME", alternate = ["companyName"])
    val companyName: String? = null,
    @SerializedName(value = "REPRESENTATIVE_NAME", alternate = ["representativeName"])
    val representativeName: String? = null,
    @SerializedName(value = "BUSINESS_NUMBER", alternate = ["businessNumber"])
    val businessNumber: String? = null,
    @SerializedName(value = "TONGSIN_NUMBER", alternate = ["tongsinNumber"])
    val tongsinNumber: String? = null,
    @SerializedName(value = "CS_PHONE", alternate = ["csPhone"])
    val csPhone: String? = null,
    @SerializedName(value = "ADDRESS", alternate = ["address"])
    val address: String? = null
)
