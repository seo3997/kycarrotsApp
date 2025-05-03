package com.whomade.kycarrots.data.api

import com.whomade.kycarrots.data.model.AdResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface AdApi {

    @POST("advertises")
    suspend fun getAdItems(
        @Query("token") token: String,
        @Query("ad_code") adCode: Int,
        @Query("pageno") pageNo: Int
    ): Response<AdResponse>
}
