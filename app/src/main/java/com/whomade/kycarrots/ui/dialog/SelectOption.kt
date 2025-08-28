package com.whomade.kycarrots.ui.dialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectOption(
    val code1: String,          // 필수 (예: buyerId, productId 등 메인 코드)
    val code2: String = "",     // 선택 (없으면 "")
    val code3: String = "",     // 선택 (없으면 "")
    val code4: String = "",     // 선택 (없으면 "")
    val code5: String = "",     // 선택 (없으면 "")
    val code6: String = "",     // 선택 (없으면 "")
    val name: String            // 필수 (UI 표시 텍스트)
) : Parcelable
