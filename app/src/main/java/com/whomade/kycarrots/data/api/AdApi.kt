package com.whomade.kycarrots.data.api

import com.whomade.kycarrots.data.model.AdResponse
import com.whomade.kycarrots.data.model.FindEmailResponse
import com.whomade.kycarrots.data.model.FindPasswordResponse
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.data.model.ProductDetailResponse
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
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

    @POST("product")
    suspend fun getAdItems(
        @Query("token") token: String,
        @Query("ad_code") adCode: Int,
        @Query("pageno") pageNo: Int
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
    suspend fun getProductDetail(@Path("productId") productId: Long): Response<ProductDetailResponse>

    @POST("product/image/delete")
    suspend fun deleteImageById(
        @Query("imageId") imageId: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("members/login")
    suspend fun login(
        @Field("id") email: String,
        @Field("pass") password: String,
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


}
