package com.whomade.kycarrots.domain.service

import com.whomade.kycarrots.common.RetrofitProvider
import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.repository.RemoteRepository

object AppServiceProvider {
    fun getService(): AppService {
        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        return AppService(repository)
    }
}
