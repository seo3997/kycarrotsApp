package com.whomade.kycarrots.domain.Helper

import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

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

    @JvmStatic
    fun registerAdvertise(
        appService: AppService,
        product: ProductVo,
        imageMetas: List<ProductImageVo>,
        images: List<File>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = appService.registerAdvertise(product, imageMetas, images)
                if (success) {
                    onSuccess()
                } else {
                    onError(Exception("광고 등록 실패"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}