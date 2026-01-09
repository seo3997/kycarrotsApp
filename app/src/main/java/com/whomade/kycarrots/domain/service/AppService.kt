package com.whomade.kycarrots.domain.service

import android.util.Log
import com.google.android.gms.common.api.Response
import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.model.AdListRequest
import com.whomade.kycarrots.data.model.ChatBuyerDto
import com.whomade.kycarrots.data.model.ChatMessageResponse
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.data.model.EmailSendReq
import com.whomade.kycarrots.data.model.EmailVerifyReq
import com.whomade.kycarrots.data.model.EmailVerifyResp
import com.whomade.kycarrots.data.model.InterestRequest
import com.whomade.kycarrots.data.model.LinkSocialRequest
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.data.model.OnboardingRequest
import com.whomade.kycarrots.data.model.OnboardingResponse
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.data.model.ProductItem
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.data.model.PurchaseHistoryRequest
import com.whomade.kycarrots.data.model.PushTokenVo
import com.whomade.kycarrots.data.model.SimpleResultResponse
import com.whomade.kycarrots.data.model.SocialAuthRequest
import com.whomade.kycarrots.data.model.SocialAuthResponse
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.loginout.SocialProvider
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import okhttp3.ResponseBody
import java.io.File

class AppService(
    private val repository: RemoteRepository
) {

    suspend fun getAdvertiseList(req: AdListRequest): List<AdItem> {
        val response = repository.fetchAdvertiseList(req)
        return if (response.isSuccessful) {
            response.body()?.items ?: emptyList()
        } else emptyList()
    }

    suspend fun getBuyAdvertiseList(req: AdListRequest): List<AdItem> {
        val response = repository.fetchBuyAdvertiseList(req)
        return if (response.isSuccessful) {
            response.body()?.items ?: emptyList()
        } else emptyList()
    }

    // 광고 등록
    suspend fun registerAdvertise(
        product: ProductVo,
        imageMetas: List<ProductImageVo>,
        images: List<File>
    ): Boolean {
        val response = repository.registerAdvertise(product, imageMetas, images)
        if (!response.isSuccessful) return false
        return response.body()?.result == true
    }

    suspend fun updateAdvertise(
        product: ProductVo,
        imageMetas: List<ProductImageVo>,
        images: List<File>
    ): Boolean {
        val response = repository.updateAdvertise(product, imageMetas, images)
        if (!response.isSuccessful) return false
        return response.body()?.result == true
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
        loginCd: String,
        regId: String,
        appVersion: String,
        providerUserId: String
    ): LoginResponse? {
        val response = repository.login(email, password, loginCd,regId, appVersion,providerUserId)
        return if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }

    suspend fun findPassword(email: String): String? {
        val response = repository.findPassword(email)
        return if (response.isSuccessful) {
            response.body()?.resultString
        } else {
            null
        }
    }

    suspend fun findEmail(name: String, phone: String): String? {
        val response = repository.findEmail(name, phone)
        return if (response.isSuccessful) response.body()?.resultString else null
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
    suspend fun registerUser(user: OpUserVO): LoginResponse? {
        val response = repository.registerUser(user)
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            null
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
        return try {
            val response = repository.registerPushToken(pushTokenVo)

            if (!response.isSuccessful) {
                false
            } else {
                response.body()?.let {
                    if (!it.result) {
                        Log.e("PushToken", "save fail: ${it.message}")
                    }
                    it.result
                } ?: false
            }
        } catch (e: Exception) {
            Log.e("PushToken", "error", e)
            false
        }
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

    suspend fun getPurchaseList(
        token: String,
        pageNo: Int
    ): List<AdItem> {
        val response = repository.fetchPurchaseList(token, pageNo)
        return if (response.isSuccessful) {
            response.body()?.items ?: emptyList()
        } else emptyList()
    }

    suspend fun getChatBuyers(
        productId: Long,
        sellerId: String
    ): List<ChatBuyerDto> {
        return try {
            val resp = repository.fetchChatBuyers(productId, sellerId)
            if (resp.isSuccessful) resp.body().orEmpty() else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * 구매이력 생성: 컨트롤러가 {result, message, id} 반환.
     * @return Pair(success, message)
     */
    suspend fun createPurchase(
        productId: Long,
        buyerNo: Long,
        roomId: String? = null,
        sellerNo: Long
    ): Pair<Boolean, String?> {
        return try {
            val resp = repository.createPurchase(
                PurchaseHistoryRequest(productId, buyerNo, roomId, sellerNo)
            )
            if (resp.isSuccessful) {
                val body = resp.body()
                (body?.result == true) to body?.message
            } else {
                false to ("요청 실패 (${resp.code()})")
            }
        } catch (e: Exception) {
            false to e.message
        }
    }

    //도매상(중간센터) 목록 조회
    suspend fun getWholesalers(memberCode: String): List<OpUserVO> {
        val resp = repository.fetchWholesalers(memberCode)
        return if (resp.isSuccessful) resp.body().orEmpty() else emptyList()
    }

    //기본 중간센터 조회 (지정 없으면 null)
    suspend fun getDefaultWholesaler(userId: String): Long? {
        val resp = repository.fetchDefaultWholesaler(userId) // Response<Long>
        return when {
            resp.code() == 204 -> null               // ✅ 204 → 미지정
            resp.isSuccessful   -> resp.body()       // 200 with number
            else                -> null
        }
    }
    //기본 중간센터 설정
    suspend fun setDefaultWholesaler(userId: String, wholesalerNo: String): Boolean {
        val resp = repository.updateDefaultWholesaler(userId, wholesalerNo)
        return resp.isSuccessful
    }

    suspend fun sendEmailCode(req: EmailSendReq) :Boolean {
        val resp = repository.sendEmailCode(req)
        return resp.isSuccessful
    }

    suspend fun verifyEmailCode(req: EmailVerifyReq): EmailVerifyResp? {
        val resp = repository.verifyEmailCode(req)
        return if (resp.isSuccessful) resp.body() else null
    }
    suspend fun postOnboarding(req: OnboardingRequest): OnboardingResponse? {
        val resp = repository.postOnboarding(req)
        return if (resp.isSuccessful) resp.body() else null
    }

    suspend fun authSocial(req: SocialAuthRequest): LoginResponse? {
        val resp = repository.authSocial(req) // 옵션 B면 authApi.authSocial("KAKAO", req)
        return if (resp.isSuccessful) resp.body() else null
    }

    fun saveJwt(jwt: String) {
        // TODO: SharedPreferences / DataStore 등에 저장
    }
    suspend fun linkSocial(req: LinkSocialRequest): LoginResponse? {
        val resp = repository.linkSocial(req) // 옵션 B면 authApi.authSocial("KAKAO", req)
        return if (resp.isSuccessful) resp.body() else null
    }

}
