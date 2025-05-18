package com.whomade.kycarrots.common

import android.content.Context
import android.util.Log
import com.whomade.kycarrots.StaticDataInfo
import com.whomade.kycarrots.data.model.FindPasswordResponse
import com.whomade.kycarrots.domain.service.AppService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FindPassword(
    private val context: Context,
    private val phone: String,
    private val email: String,
    private val appService: AppService
) {

    suspend fun find(): Int = withContext(Dispatchers.IO) {
        try {
            val password = appService.findPassword(phone, email)

            return@withContext if (!password.isNullOrBlank()) {
                saveTempPassword(password)
                StaticDataInfo.RESULT_CODE_200
            } else {
                StaticDataInfo.RESULT_NO_USER
            }

        } catch (e: Exception) {
            Log.e("FindPassword", "Exception during find(): ${e.message}", e)
            return@withContext StaticDataInfo.RESULT_CODE_ERR
        }
    }

    private fun saveTempPassword(tempPassword: String) {
        context.getSharedPreferences("TempPasswordInfo", Context.MODE_PRIVATE).edit().apply {
            putString("TempPassword", tempPassword)
            apply()
        }
    }
}
