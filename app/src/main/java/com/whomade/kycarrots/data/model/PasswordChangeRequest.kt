package com.whomade.kycarrots.data.model

data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)
