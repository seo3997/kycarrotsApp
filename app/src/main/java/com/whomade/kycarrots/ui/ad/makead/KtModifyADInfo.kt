package com.whomade.kycarrots.ui.ad.makead

/**
 * 광고 수정 시 data (Java ModifyADInfo -> Kotlin)
 */
data class KtModifyADInfo(
    var productId: String? = null,
    var userNo: String? = null,
    var title: String? = null,
    var description: String? = null,
    var price: String? = null,
    var categoryGroup: String? = null,
    var categoryMid: String? = null,
    var categoryScls: String? = null,
    var saleStatus: String? = null,
    var registerNo: String? = null,
    var updusrNo: String? = null,
    var areaGroup: String? = null,
    var areaMid: String? = null,
    var areaScls: String? = null,
    var quantity: String? = null,
    var unitGroup: String? = null,
    var unitCode: String? = null,
    var desiredShippingDate: String? = null,

    // 이미지 관련 (수정 모드용)
    var aDTitleimageId: String? = null,
    var strADTitleImgUrl: String? = null,
    var strADDetailImgUrl1: String? = null,
    var strADDetailImgUrl2: String? = null,
    var strADDetailImgUrl3: String? = null,
    var aDDetailimageId1: String? = null,
    var aDDetailimageId2: String? = null,
    var aDDetailimageId3: String? = null
)
