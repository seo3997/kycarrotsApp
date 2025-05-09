package com.whomade.kycarrots.data.model

data class ProductDetailResponse(
    val product: ProductVo,
    val imageMetas: List<ProductImageVo>
)
