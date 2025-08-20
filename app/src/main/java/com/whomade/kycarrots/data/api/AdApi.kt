package com.whomade.kycarrots.data.api

import com.whomade.kycarrots.data.model.AdResponse
import com.whomade.kycarrots.data.model.ChatMessageResponse
import com.whomade.kycarrots.data.model.ChatRoomResponse
import com.whomade.kycarrots.data.model.FindEmailResponse
import com.whomade.kycarrots.data.model.FindPasswordResponse
import com.whomade.kycarrots.data.model.InterestRequest
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.data.model.OpUserVO
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.data.model.ProductItem
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.data.model.PushTokenVo
import com.whomade.kycarrots.data.model.SimpleResultResponse
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
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AdApi {

    @GET("common/codelist")
    suspend fun getCodeList(
        @Query("groupId") groupId: String
    ): Response<List<TxtListDataInfo>>

    @GET("common/sCodeList")
    suspend fun getSCodeList(
        @Query("groupId") groupId: String,
        @Query("mcode") mcode: String
    ): Response<List<TxtListDataInfo>>

    @POST("product")
    suspend fun getAdItems(
        @Query("token") token: String,
        @Query("ad_code") adCode: Int,
        @Query("pageno") pageNo: Int,
        @Query("categoryGroup")   categoryGroup: String? = "R010610",
        @Query("categoryMid")     categoryMid:   String? = null,
        @Query("categoryScls")    categoryScls:  String? = null,
        @Query("areaGroup")       areaGroup:     String? = "R010070",
        @Query("areaMid")         areaMid:       String? = null,
        @Query("areaScls")        areaScls:      String? = null,
        @Query("minPrice")        minPrice:      Int?    = null,
        @Query("maxPrice")        maxPrice:      Int?    = null
    ): Response<AdResponse>

    @Multipart
    @POST("product/register")
    suspend fun registerAdvertise(
        @Part("product") product: RequestBody,
        @Part("imageMetas") imageMetas: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @Multipart
    @POST("product/update")
    suspend fun updateAdvertise(
        @Part("product") product: RequestBody,
        @Part("imageMetas") imageMetas: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @GET("product/detail/{productId}")
    suspend fun getProductDetail(@Path("productId") productId: Long,
                                 @Query("userNo") userNo: Long): Response<ProductDetailResponse>

    @POST("product/image/delete")
    suspend fun deleteImageById(
        @Query("imageId") imageId: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("members/login")
    suspend fun login(
        @Field("id") email: String,
        @Field("pass") password: String,
        @Field("member_code") memberCode: String,
        @Field("reg_id") regId: String,
        @Field("appver") appVersion: String
    ): Response<LoginResponse>

    @GET("user/find-password")
    suspend fun findPassword(
        @Query("hp") hp: String,
        @Query("mail") mail: String
    ): Response<FindPasswordResponse>

    @GET("user/find-email")
    suspend fun findEmail(
        @Query("nm") name: String,
        @Query("birth") birthDate: String,
        @Query("hp") phone: String
    ): Response<FindEmailResponse>

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
    @POST("members/email-check")
    suspend fun checkEmailDuplicate(
        @Field("email") email: String
    ): Response<SimpleResultResponse>

    @POST("members/register")
    suspend fun registerUser(@Body user: OpUserVO): Response<SimpleResultResponse>

    @FormUrlEncoded
    @POST("members/userinfo")
    suspend fun getUserInfoByToken(
        @Field("token") token: String
    ): Response<OpUserVO>

    @GET("product/dashboard")
    suspend fun getProductDashboard(
        @Query("token") token: String
    ): Response<Map<String, Int>>

    @GET("product/recent")
    suspend fun getRecentProducts(
        @Query("token") token: String
    ): Response<List<ProductVo>>

    @POST("/members/push/savetoken")
    suspend fun registerPushToken(
        @Body request: PushTokenVo
    ): Response<Void>

    @POST("product/status/update")
    suspend fun updateProductStatus(
        @Query("token") token: String,
        @Body product: ProductItem
    ): Response<ResponseBody>

    @POST("api/interests/toggle")
    suspend fun toggle(@Body req: InterestRequest): Response<Boolean>

    @GET("product/interests/list")
    suspend fun getInterestItems(
        @Query("token") token: String,
        @Query("pageno") pageNo: Int
    ): Response<AdResponse>
}
