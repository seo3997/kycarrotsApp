package com.whomade.kycarrots.data.model

data class LinkSocialRequest (
    val userId: String,
    val userNo: String,
    val provider: String? = null,
    val providerUserId: String? = null
)