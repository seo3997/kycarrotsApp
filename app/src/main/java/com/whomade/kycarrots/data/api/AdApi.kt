package com.whomade.kycarrots.data.api

import com.whomade.kycarrots.data.model.AdListRequest
import com.whomade.kycarrots.data.model.AdResponse
import com.whomade.kycarrots.data.model.ChatBuyerDto
import com.whomade.kycarrots.data.model.ChatMessageResponse
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.data.model.EmailSendReq
import com.whomade.kycarrots.data.model.EmailVerifyReq
import com.whomade.kycarrots.data.model.EmailVerifyResp
import com.whomade.kycarrots.data.model.StringResponse
import com.whomade.kycarrots.data.model.InterestRequest
import com.whomade.kycarrots.data.model.LinkSocialRequest
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.data.model.OnboardingRequest
import com.whomade.kycarrots.data.model.OnboardingResponse
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductItem
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.data.model.PurchaseHistoryRequest
import com.whomade.kycarrots.data.model.PushTokenVo
import com.whomade.kycarrots.data.model.SimpleResult
import com.whomade.kycarrots.data.model.SimpleResultResponse
import com.whomade.kycarrots.data.model.SocialAuthRequest
import com.whomade.kycarrots.data.model.SocialAuthResponse
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AdApi {

    @GET("api/common/codelist")
    suspend fun getCodeList(
        @Query("groupId") groupId: String
    ): Response<List<TxtListDataInfo>>

    @GET("api/common/sCodeList")
    suspend fun getSCodeList(
        @Query("groupId") groupId: String,
        @Query("mcode") mcode: String
    ): Response<List<TxtListDataInfo>>

    @POST("api/product")
    suspend fun getAdItems(
        @Body req: AdListRequest
    ): Response<AdResponse>

    @POST("api/product/buyListAdvertise")
    suspend fun getBuyAdItems(
        @Body req: AdListRequest
    ): Response<AdResponse>

    @Multipart
    @POST("api/product/register")
    suspend fun registerAdvertise(
        @Part("product") product: RequestBody,
        @Part("imageMetas") imageMetas: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<SimpleResultResponse>

    @Multipart
    @POST("api/product/update")
    suspend fun updateAdvertise(
        @Part("product") product: RequestBody,
        @Part("imageMetas") imageMetas: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<SimpleResultResponse>

    @GET("api/product/detail/{productId}")
    suspend fun getProductDetail(@Path("productId") productId: Long,
                                 @Query("userNo") userNo: Long): Response<ProductDetailResponse>

    @POST("api/product/image/delete")
    suspend fun deleteImageById(
        @Query("imageId") imageId: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("api/members/login")
    suspend fun login(
        @Field("id") email: String,
        @Field("pass") password: String,
        @Field("login_cd") loginCd: String,
        @Field("reg_id") regId: String,
        @Field("appver") appVersion: String,
        @Field("providerUserId") providerUserId: String,
    ): Response<LoginResponse>

    @GET("api/members/find-password")
    suspend fun findPassword(
        @Query("mail") mail: String
    ): Response<StringResponse>

    @GET("api/members/find-email")
    suspend fun findEmail(
        @Query("nm") name: String,
        @Query("hp") phone: String
    ): Response<StringResponse>

    @POST("/api/chat/room")
    suspend fun createOrGetChatRoom(
        @Query("productId") productId: String,
        @Query("buyerId") buyerId: String,
        @Query("sellerId") sellerId: String
    ): Response<ChatRoomResponse>

    @GET("/api/chat/rooms/{productId}/{userId}")
    suspend fun getUserChatRooms(
        @Path("productId") productId: String,
        @Path("userId") userId: String
    ): Response<List<ChatRoomResponse>>

    @GET("/api/chatmessage/list/{roomId}")
    suspend fun getChatMessages(@Path("roomId") roomId: String): Response<List<ChatMessageResponse>>

    @FormUrlEncoded
    @POST("api/members/email-check")
    suspend fun checkEmailDuplicate(
        @Field("email") email: String
    ): Response<SimpleResultResponse>

    @POST("api/members/register")
    suspend fun registerUser(@Body user: OpUserVO): Response<LoginResponse>

    @FormUrlEncoded
    @POST("api/members/userinfo")
    suspend fun getUserInfoByToken(
        @Field("token") token: String
    ): Response<OpUserVO>

    @GET("api/product/dashboard")
    suspend fun getProductDashboard(
        @Query("token") token: String
    ): Response<Map<String, Int>>

    @GET("api/product/recent")
    suspend fun getRecentProducts(
        @Query("token") token: String
    ): Response<List<ProductVo>>

    @POST("api/members/push/savetoken")
    suspend fun registerPushToken(
        @Body request: PushTokenVo
    ): Response<SimpleResult>

    @POST("api/product/status/update")
    suspend fun updateProductStatus(
        @Query("token") token: String,
        @Body product: ProductItem
    ): Response<SimpleResultResponse>

    @POST("api/interests/toggle")
    suspend fun toggle(@Body req: InterestRequest): Response<Boolean>

    @GET("api/product/interests/list")
    suspend fun getInterestItems(
        @Query("token") token: String,
        @Query("pageno") pageNo: Int
    ): Response<AdResponse>

    @GET("api/product/purchases/list") // 또는 "/api/purchases/list" (프로젝트 baseUrl 구성에 맞게)
    suspend fun getPurchaseItems(
        @Query("token") token: String,
        @Query("pageno") pageNo: Int
    ): Response<AdResponse>

    @GET("api/product/chat/buyers")
    suspend fun getChatBuyers(
        @Query("productId") productId: Long,
        @Query("sellerId") sellerId: String
    ): Response<List<ChatBuyerDto>>

    @POST("api/purchases")
    suspend fun createPurchase(
        @Body body: PurchaseHistoryRequest
    ): Response<SimpleResult>

    // 도매상(중간센터) 목록 조회
    @GET("api/members/wholesalers")
    suspend fun getWholesalers(
        @Query("memberCode") memberCode: String
    ): Response<List<OpUserVO>>

    //사용자 기본 중간센터 조회 (Long 하나만 반환)
    @GET("api/members/default-wholesaler")
    suspend fun getDefaultWholesaler(
        @Query("userId") userId: String
    ): Response<Long>

    // 사용자 기본 중간센터 설정
    @POST("api/members/default-wholesaler")
    suspend fun setDefaultWholesaler(
        @Query("userId") userId: String,
        @Query("wholesalerNo") wholesalerNo: String
    ): Response<Void>

    @POST("api/email/send-code")
    suspend fun sendEmailCode(@Body req: EmailSendReq): Response<Void>

    @POST("api/email/verify-code")
    suspend fun verifyEmailCode(@Body req: EmailVerifyReq):Response<EmailVerifyResp>

    @POST("api/user/onboarding")
    suspend fun postOnboarding(@Body req: OnboardingRequest): Response<OnboardingResponse>

    @POST("api/members/social")
    suspend fun authSocial(@Body req: SocialAuthRequest): Response<LoginResponse>

    @POST("api/members/link")
    suspend fun linkSocial(@Body req: LinkSocialRequest): Response<LoginResponse>
}
