package com.whomade.kycarrots.domain.service

import com.whomade.kycarrots.data.model.AdItem
import com.whomade.kycarrots.data.repository.RemoteRepository

class AppService(
    private val repository: RemoteRepository
) {
    suspend fun getAdvertiseList(token: String, adCode: Int, pageNo: Int): List<AdItem> {
        val response = repository.fetchAdvertiseList(token, adCode, pageNo)
        return if (response.isSuccessful) {
            response.body()?.items ?: emptyList()
        } else {
            emptyList() // 혹은 throw Exception("API Error: ${response.code()}")
        }
    }
}
