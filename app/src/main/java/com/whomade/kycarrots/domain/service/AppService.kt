package com.whomade.kycarrots.domain.service

import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import okhttp3.ResponseBody
import java.io.File

class AppService(
    private val repository: RemoteRepository
) {
    suspend fun getAdvertiseList(token: String, adCode: Int, pageNo: Int): List<AdItem> {
        val response = repository.fetchAdvertiseList(token, adCode, pageNo)
        return if (response.isSuccessful) {
            response.body()?.items ?: emptyList()
        } else {
            emptyList() // 혹은 throw Exception("API Error: ${response.code()}")
        }
    }

    // 광고 등록
    suspend fun registerAdvertise(
        product: ProductVo,
        imageMetas: List<ProductImageVo>,
        images: List<File>
    ): Boolean {
        val response = repository.registerAdvertise(product, imageMetas, images)
        return response.isSuccessful
    }

    suspend fun updateAdvertise(
        product: ProductVo,
        imageMetas: List<ProductImageVo>,
        images: List<File>
    ): Boolean {
        val response = repository.updateAdvertise(product, imageMetas, images)
        return response.isSuccessful
    }

    // 코드 리스트 조회
    suspend fun getCodeList(groupId: String): List<TxtListDataInfo> {
        val response = repository.fetchCodeList(groupId)
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList() // 또는 throw Exception("API Error: ${response.code()}")
        }
    }

    suspend fun getProductDetail(productId: Long): ProductDetailResponse? {
        val response = repository.fetchProductDetail(productId)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun deleteImageById(imageId: String): retrofit2.Response<ResponseBody> {
        return repository.deleteImageById(imageId)
    }

    suspend fun login(
        email: String,
        password: String,
        regId: String,
        appVersion: String
    ): LoginResponse? {
        val response = repository.login(email, password, regId, appVersion)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun findPassword(phone: String, email: String): String? {
        val response = repository.findPassword(phone, email)
        return if (response.isSuccessful) {
            response.body()?.password
        } else {
            null
        }
    }

    suspend fun findEmail(name: String, birth: String, phone: String): String? {
        val response = repository.findEmail(name, birth, phone)
        return if (response.isSuccessful) response.body()?.email else null
    }

}
