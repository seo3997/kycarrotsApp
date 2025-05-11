package com.whomade.kycarrots.domain.Helper

import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    @JvmStatic
    fun updateAdvertise(
        appService: AppService,
        product: ProductVo,
        imageMetas: List<ProductImageVo>,
        images: List<File>,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = appService.updateAdvertise(product, imageMetas, images)
                if (success) {
                    onSuccess()
                } else {
                    onError(Exception("광고 수정 실패"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
    @JvmStatic
    fun getProductDetailAsync(
        appService: AppService,
        productId: Long,
        onSuccess: (ProductDetailResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = appService.getProductDetail(productId)
                if (result != null) {
                    onSuccess(result)
                } else {
                    onError(Exception("결과 없음"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    @JvmStatic
    fun deleteImageById(
        appService: AppService,
        imageId: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val response = appService.deleteImageById(imageId)
                if (response.isSuccessful) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onError(Exception("서버 오류: ${response.code()}"))
                    }
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}