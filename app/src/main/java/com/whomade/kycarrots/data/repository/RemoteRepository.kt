package com.whomade.kycarrots.data.repository

import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.model.AdResponse
import retrofit2.Response

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

    // 여기에 다른 API들도 추가 가능
    // suspend fun fetchUserInfo(...) : Response<User>
    // suspend fun fetchNotices(...) : Response<NoticeList>
}
