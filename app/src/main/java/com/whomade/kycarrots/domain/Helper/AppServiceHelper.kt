package com.whomade.kycarrots.domain.Helper

import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppServiceHelper {
    @JvmStatic
    fun fetchCodeList(
        appService: AppService,
        groupId: String,
        onSuccess: OnCodeListSuccess,
        onError: OnCodeListError
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = appService.getCodeList(groupId)
                onSuccess.onSuccess(result)
            } catch (e: Exception) {
                onError.onError(e)
            }
        }
    }
}