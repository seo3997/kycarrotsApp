package com.whomade.kycarrots.data.model

import com.google.gson.annotations.SerializedName

data class AdItem(
    @SerializedName(value = "productId", alternate = ["PRODUCT_ID"])
    val productId: String,
    @SerializedName(value = "title", alternate = ["TITLE"])
    val title: String,
    @SerializedName(value = "description", alternate = ["DESCRIPTION"])
    val description: String,
    @SerializedName(value = "price", alternate = ["PRICE"])
    val price: String,
    @SerializedName(value = "imageUrl", alternate = ["IMAGE_URL"])
    val imageUrl: String,
    @SerializedName(value = "userId", alternate = ["USER_NO"])
    val userId: String,
)