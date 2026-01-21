// UnlinkSocialRequest.kt
package com.whomade.kycarrots.data.model

data class UnlinkSocialRequest(
    val provider: String,        // "KAKAO", "NAVER", "GOOGLE" ...
    val providerUserId: String   // kakao user id 등
)
