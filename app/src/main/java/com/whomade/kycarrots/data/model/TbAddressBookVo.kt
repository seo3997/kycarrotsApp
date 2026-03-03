package com.whomade.kycarrots.data.model

data class TbAddressBookVo(
    val addressId: Long? = null,
    val userNo: String? = null,
    val recipientName: String? = null,
    val recipientPhone: String? = null,
    val zipCode: String? = null,
    val addressMain: String? = null,
    val addressDetail: String? = null,
    val isDefault: Int? = 0,
    val memo: String? = null
)
