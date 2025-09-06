package com.whomade.kycarrots.ui.dialog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BottomDtoParcel(
    val code: String,
    val name: String,
    val text1: String? = null,
    val text2: String? = null,
    val text3: String? = null,
    val text4: String? = null
) : Parcelable {
    fun toDto() = BottomDto(code, name, text1, text2, text3, text4)

    companion object {
        fun from(d: BottomDto) =
            BottomDtoParcel(d.code, d.name, d.text1, d.text2, d.text3, d.text4)
    }
}
