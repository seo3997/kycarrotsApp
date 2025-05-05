package com.whomade.kycarrots.common

import com.whomade.kycarrots.data.api.AdApi
import com.whomade.kycarrots.data.repository.RemoteRepository
import com.whomade.kycarrots.domain.service.AppService

object AppServiceProvider {
    val instance: AppService by lazy {
        val adApi = RetrofitProvider.retrofit.create(AdApi::class.java)
        val repository = RemoteRepository(adApi)
        AppService(repository)
    }
}
