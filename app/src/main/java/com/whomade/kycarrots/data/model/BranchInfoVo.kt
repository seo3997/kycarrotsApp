package com.whomade.kycarrots.data.model

import com.google.gson.annotations.SerializedName

data class BranchInfoVo(
    @SerializedName("BRANCH_ID")
    val branchId: Long = 0,
    @SerializedName("BRANCH_CODE")
    val branchCode: String? = null,
    @SerializedName("BRANCH_NAME")
    val branchName: String? = null,
    @SerializedName("LOGO_IMAGE_URL")
    val logoImageUrl: String? = null,
    @SerializedName("BRANCH_STATUS")
    val branchStatus: String? = null,
    @SerializedName("TOSS_CLIENT_KEY")
    val tossClientKey: String? = null,
    @SerializedName("BANK_CD")
    val bankCd: String? = null,
    @SerializedName("ACCOUNT_NO")
    val accountNo: String? = null,
    @SerializedName("ACCOUNT_HOLDER")
    val accountHolder: String? = null,
    @SerializedName("BASE_SHIPPING_FEE")
    val baseShippingFee: Int = 0,
    @SerializedName("FREE_SHIPPING_THRESHOLD")
    val freeShippingThreshold: Int = 0,
    @SerializedName("EXTRA_SHIPPING_FEE")
    val extraShippingFee: Int = 0,
    @SerializedName("IS_USE_CUSTOM_PRICE")
    val isUseCustomPrice: Int = 0,
    @SerializedName("COMPANY_NAME")
    val companyName: String? = null,
    @SerializedName("REPRESENTATIVE_NAME")
    val representativeName: String? = null,
    @SerializedName("BUSINESS_NUMBER")
    val businessNumber: String? = null,
    @SerializedName("TONGSIN_NUMBER")
    val tongsinNumber: String? = null,
    @SerializedName("CS_PHONE")
    val csPhone: String? = null,
    @SerializedName("ADDRESS")
    val address: String? = null
)
