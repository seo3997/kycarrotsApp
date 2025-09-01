package com.whomade.kycarrots.data.repository

import com.google.gson.Gson
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.AdListRequest
import com.whomade.kycarrots.data.model.AdResponse
import com.whomade.kycarrots.data.model.ChatBuyerDto
import com.whomade.kycarrots.data.model.ChatMessageResponse
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.data.model.FindEmailResponse
import com.whomade.kycarrots.data.model.FindPasswordResponse
import com.whomade.kycarrots.data.model.InterestRequest
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.data.model.ProductItem
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.data.model.PurchaseHistoryRequest
import com.whomade.kycarrots.data.model.PushTokenVo
import com.whomade.kycarrots.data.model.SimpleResult
import com.whomade.kycarrots.data.model.SimpleResultResponse
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
    suspend fun fetchAdvertiseList(req: AdListRequest): Response<AdResponse> {
        return adApi.getAdItems(req)
    }

    suspend fun fetchBuyAdvertiseList(req: AdListRequest): Response<AdResponse> {
        return adApi.getBuyAdItems(req)
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

    suspend fun fetchSCodeList(groupId: String, mcode: String): Response<List<TxtListDataInfo>> {
        return adApi.getSCodeList(groupId,mcode)
    }

    suspend fun fetchProductDetail(productId: Long,UserNO: Long): Response<ProductDetailResponse> {
        return adApi.getProductDetail(productId,UserNO)
    }

    suspend fun deleteImageById(imageId: String): Response<ResponseBody> {
        return adApi.deleteImageById(imageId)
    }

    suspend fun login(
        email: String,
        password: String,
        memberCode: String,
        regId: String,
        appVersion: String
    ): Response<LoginResponse> {
        return adApi.login(email, password,memberCode, regId, appVersion)
    }

    suspend fun findPassword(
        phone: String,
        email: String
    ): Response<FindPasswordResponse> {
        return adApi.findPassword(phone, email)
    }

    suspend fun findEmail(
        name: String,
        birth: String,
        phone: String
    ): Response<FindEmailResponse> {
        return adApi.findEmail(name, birth, phone)
    }
    // 채팅방 생성
    suspend fun createOrGetChatRoom(
        productId: String,
        buyerId: String,
        sellerId: String
    ): Response<ChatRoomResponse> {
        return adApi.createOrGetChatRoom(productId, buyerId ,sellerId)
    }

    // 채팅방 목록 조회
    suspend fun getUserChatRooms(
        productId: String,
        userId: String
    ): Response<List<ChatRoomResponse>> {
        return adApi.getUserChatRooms(productId,userId)
    }
    suspend fun getChatMessages(roomId: String): Response<List<ChatMessageResponse>> {
        return adApi.getChatMessages(roomId)
    }
    // 회원가입
    suspend fun registerUser(user: OpUserVO): Response<SimpleResultResponse> {
        return adApi.registerUser(user)
    }

    // 이메일 중복 체크
    suspend fun checkEmailDuplicate(email: String): Response<SimpleResultResponse> {
        return adApi.checkEmailDuplicate(email)
    }

    suspend fun getUserInfoByToken(token: String): Response<OpUserVO> {
        return adApi.getUserInfoByToken(token)
    }

    suspend fun fetchProductDashboard(token: String): Response<Map<String, Int>> {
        return adApi.getProductDashboard(token)
    }

    suspend fun fetchRecentProducts(token: String): Response<List<ProductVo>> {
        return adApi.getRecentProducts(token)
    }

    suspend fun registerPushToken(pushTokenVo: PushTokenVo): Response<Void> {
        return adApi.registerPushToken(pushTokenVo)
    }

    suspend fun updateProductStatus(token: String, product: ProductItem): Response<ResponseBody> {
        return adApi.updateProductStatus(token, product)
    }
    suspend fun toggleInterest(req: InterestRequest): Response<Boolean> {
        return adApi.toggle(req)
    }
    suspend fun fetchInterestList(
        token: String,
        pageNo: Int
    ): Response<AdResponse> {
        return adApi.getInterestItems(token, pageNo)
    }

    suspend fun fetchPurchaseList(
        token: String,
        pageNo: Int
    ): Response<AdResponse> {
        return adApi.getPurchaseItems(token, pageNo)
    }

    suspend fun fetchChatBuyers(
        productId: Long,
        sellerId: String
    ): Response<List<ChatBuyerDto>> {
        return adApi.getChatBuyers(productId, sellerId)
    }
    suspend fun createPurchase(
        body: PurchaseHistoryRequest
    ): Response<SimpleResult> = adApi.createPurchase(body)
}
