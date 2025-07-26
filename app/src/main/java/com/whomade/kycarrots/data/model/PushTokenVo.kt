package com.whomade.kycarrots.data.model

data class PushTokenVo(
    val userId: String,
    val pushToken: String,
    val deviceType: String = "ANDROID"
)
