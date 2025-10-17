package com.whomade.kycarrots.data.model// package com.whomade.kycarrots.data.dto

data class EmailSendReq(
    val email: String
)

data class EmailVerifyReq(
    val email: String,
    val code: String
)

data class EmailVerifyResp(
    val verified: Boolean
)

// 온보딩 요청/응답 (없으면 같이 생성)
data class OnboardingRequest(
    val nickname: String,
    val email: String,
    val role: String,            // ROLE_PUB / ROLE_SELL / ROLE_PROJ
    val areaGroup: String?,
    val areaMid: String?,
    val areaScls: String?,
    val marketingPush: Boolean,
    val marketingEmail: Boolean,
    val tosAgreed: Boolean,
    val privacyAgreed: Boolean
)

data class OnboardingResponse(
    val userId: Long,
    val role: String
)




