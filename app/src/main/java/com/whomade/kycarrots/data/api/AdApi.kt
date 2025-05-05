package com.whomade.kycarrots.data.api

import com.whomade.kycarrots.data.model.AdResponse
import com.whomade.kycarrots.ui.common.TxtListDataInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AdApi {

    @GET("common/codelist")
    suspend fun getCodeList(
        @Query("groupId") groupId: String
    ): Response<List<TxtListDataInfo>>

    @POST("advertises")
    suspend fun getAdItems(
        @Query("token") token: String,
        @Query("ad_code") adCode: Int,
        @Query("pageno") pageNo: Int
    ): Response<AdResponse>

    @Multipart
    @POST("advertise/register")
    suspend fun registerAdvertise(
        @Part("product") product: RequestBody,
        @Part("imageMetas") imageMetas: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ResponseBody>


}
