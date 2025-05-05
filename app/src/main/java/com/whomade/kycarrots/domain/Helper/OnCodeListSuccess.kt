package com.whomade.kycarrots.domain.Helper

import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlin.jvm.JvmSuppressWildcards

@JvmSuppressWildcards
fun interface OnCodeListSuccess {
    fun onSuccess(list: List<TxtListDataInfo>)
}

fun interface OnCodeListError {
    fun onError(t: Throwable)
}