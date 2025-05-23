package com.whomade.kycarrots.data.model

data class LoginResponse(
    val resultCode: Int = 0,
    val token: String? = null,
    val login_idx: String? = null,
    val login_si: String? = null,
    val login_gu: String? = null,
    val login_sex: String? = null,
    val login_age: String? = null
)