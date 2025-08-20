package com.whomade.kycarrots.domain.service

import com.google.android.gms.common.api.Response
import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.model.ChatMessageResponse
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.data.model.InterestRequest
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.data.model.ProductItem
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.data.model.PushTokenVo
import com.whomade.kycarrots.data.model.SimpleResultResponse
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import okhttp3.ResponseBody
import java.io.File

class AppService(
    private val repository: RemoteRepository
) {
    suspend fun getAdvertiseList(token: String,
                                 adCode: Int,
                                 pageNo: Int,
                                 categoryGroup: String?  = "R010610",
                                 categoryMid: String?    = null,
                                 categoryScls: String?   = null,
                                 areaGroup: String?      = "R010070",
                                 areaMid: String?        = null,
                                 areaScls: String?       = null,
                                 minPrice: Int?          = null,
                                 maxPrice: Int?          = null
    ): List<AdItem> {
        val response = repository.fetchAdvertiseList(
            token, adCode, pageNo,
            categoryGroup, categoryMid, categoryScls,
            areaGroup, areaMid, areaScls,
            minPrice, maxPrice
        )
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
    // 코드 리스트 조회
    suspend fun getSCodeList(groupId: String, mcode:String): List<TxtListDataInfo> {
        val response = repository.fetchSCodeList(groupId,mcode)
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList() // 또는 throw Exception("API Error: ${response.code()}")
        }
    }

    suspend fun getProductDetail(productId: Long,UserNo: Long): ProductDetailResponse? {
        val response = repository.fetchProductDetail(productId,UserNo)
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
        memberCode: String,
        regId: String,
        appVersion: String
    ): LoginResponse? {
        val response = repository.login(email, password, memberCode,regId, appVersion)
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

    suspend fun createOrGetChatRoom(
        productId: String,
        buyerId: String,
        sellerId: String
    ): ChatRoomResponse? {
        val response = repository.createOrGetChatRoom(productId, buyerId, sellerId)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun getUserChatRooms(productId: String,userId: String): List<ChatRoomResponse> {
        val response = repository.getUserChatRooms(productId,userId)
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun getChatMessages(roomId: String): List<ChatMessageResponse> {
        val response = repository.getChatMessages(roomId)
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    // 이메일 중복 체크
    suspend fun checkEmailDuplicate(email: String): SimpleResultResponse {
        val response = repository.checkEmailDuplicate(email)
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            SimpleResultResponse(result = false, message = "서버 응답 오류")
        }
    }

    suspend fun getUserInfo(token: String): OpUserVO? {
        val response = repository.getUserInfoByToken(token)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    // 회원가입
    suspend fun registerUser(user: OpUserVO): SimpleResultResponse {
        val response = repository.registerUser(user)
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            SimpleResultResponse(result = false, message = "서버 응답 오류")
        }
    }

    suspend fun getProductDashboard(token: String): Map<String, Int> {
        val response = repository.fetchProductDashboard(token)
        return if (response.isSuccessful) {
            response.body() ?: emptyMap()
        } else {
            emptyMap()
        }
    }

    suspend fun getRecentProducts(token: String): List<ProductVo> {
        val response = repository.fetchRecentProducts(token)
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun registerPushToken(pushTokenVo: PushTokenVo): Boolean {
        val response = repository.registerPushToken(pushTokenVo)
        return response.isSuccessful
    }

    suspend fun updateProductStatus(token: String, product: ProductItem): Boolean {
        val response = repository.updateProductStatus(token, product)
        return response.isSuccessful
    }

    suspend fun toggleInterest(req: InterestRequest): Boolean {
        val response = repository.toggleInterest(req)
        return response.isSuccessful
    }

    suspend fun getInterestList(
        token: String,
        pageNo: Int
    ): List<AdItem> {
        val response = repository.fetchInterestList(token, pageNo)
        return if (response.isSuccessful) {
            response.body()?.items ?: emptyList()
        } else {
            emptyList()
        }
    }

}
