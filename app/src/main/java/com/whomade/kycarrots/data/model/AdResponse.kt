package com.whomade.kycarrots.data.model

import com.google.gson.annotations.SerializedName

data class AdResponse(
    @SerializedName(value = "items", alternate = ["content"])
    val items: List<AdItem>
)