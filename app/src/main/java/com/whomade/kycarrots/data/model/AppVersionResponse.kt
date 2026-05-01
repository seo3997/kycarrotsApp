package com.whomade.kycarrots.data.model

data class AppVersionResponse(
    val success: Boolean,
    val updateType: String?, // NONE, OPTIONAL, FORCE
    val latestVersion: String?,
    val minVersion: String?,
    val updateMsg: String?,
    val storeUrl: String?,
    val message: String?
)
