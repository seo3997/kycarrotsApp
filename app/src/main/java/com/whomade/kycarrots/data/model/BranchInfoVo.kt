package com.whomade.kycarrots.data.model

data class BranchInfoVo(
    val branch_id: Long = 0,
    val branch_code: String? = null,
    val branch_name: String? = null,
    val logo_image_url: String? = null,
    val branch_status: String? = null,
    val toss_client_key: String? = null,
    val bank_cd: String? = null,
    val account_no: String? = null,
    val account_holder: String? = null,
    val base_shipping_fee: Int = 0,
    val free_shipping_threshold: Int = 0,
    val extra_shipping_fee: Int = 0,
    val is_use_custom_price: Int = 0,
    val company_name: String? = null,
    val representative_name: String? = null,
    val business_number: String? = null,
    val tongsin_number: String? = null,
    val cs_phone: String? = null,
    val address: String? = null
)
