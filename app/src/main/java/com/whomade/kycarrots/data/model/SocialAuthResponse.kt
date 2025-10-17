package com.whomade.kycarrots.data.model

data class SocialAuthResponse(
    val needOnboarding: Boolean,
    val needEmail: Boolean,
    val jwt: String?,
    val userId: String?,   // 서버 설계에 맞게
    val userNo: Long?,     // 있으면
    val provider: String?, // echo
    val message: String? = null
)