package com.whomade.kycarrots.data.repository

import com.google.gson.Gson
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.AdResponse
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

class RemoteRepository(
    private val adApi: AdApi
) {
    suspend fun fetchAdvertiseList(
        token: String,
        adCode: Int,
        pageNo: Int
    ): Response<AdResponse> {
        return adApi.getAdItems(token, adCode, pageNo)
    }

    // 광고 등록
    suspend fun registerAdvertise(
        product: ProductVo,
        imageMetas: List<ProductImageVo>,
        images: List<File>
    ): Response<ResponseBody> {
        val gson = Gson()

        // 1. JSON 문자열 → RequestBody로 변환
        val productJson = gson.toJson(product)
        val imageMetasJson = gson.toJson(imageMetas)

        val productBody = productJson.toRequestBody("application/json; charset=utf-8".toMediaType())
        val imageMetasBody = imageMetasJson.toRequestBody("application/json; charset=utf-8".toMediaType())

        // 2. 이미지 파일 리스트를 MultipartBody.Part로 변환
        val imageParts = images.map { file ->
            val requestFile = file.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        }

        // 3. API 호출
        return adApi.registerAdvertise(productBody, imageMetasBody, imageParts)
    }

    suspend fun updateAdvertise(
        product: ProductVo,
        imageMetas: List<ProductImageVo>,
        images: List<File>
    ): Response<ResponseBody> {
        val gson = Gson()

        val productJson = gson.toJson(product)
        val imageMetasJson = gson.toJson(imageMetas)

        val productBody = productJson.toRequestBody("application/json; charset=utf-8".toMediaType())
        val imageMetasBody = imageMetasJson.toRequestBody("application/json; charset=utf-8".toMediaType())

        val imageParts = images.map { file ->
            val requestFile = file.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        }

        return adApi.updateAdvertise(productBody, imageMetasBody, imageParts)
    }

    suspend fun fetchCodeList(groupId: String): Response<List<TxtListDataInfo>> {
        return adApi.getCodeList(groupId)
    }

    suspend fun fetchProductDetail(productId: Long): Response<ProductDetailResponse> {
        return adApi.getProductDetail(productId)
    }

    suspend fun deleteImageById(imageId: String): Response<ResponseBody> {
        return adApi.deleteImageById(imageId)
    }
}
