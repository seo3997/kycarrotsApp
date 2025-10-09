package com.whomade.kycarrots.common

import android.content.Context
import android.util.Log
import com.whomade.kycarrots.StaticDataInfo
import com.whomade.kycarrots.domain.service.AppService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FindPassword(
    private val context: Context,
    private val email: String,
    private val appService: AppService
) {

    suspend fun find(): Int = withContext(Dispatchers.IO) {
        try {
            // 서버 응답: "200","601","602","604"... (null 또는 비정상이면 에러로 처리)
            val codeStr = appService.findPassword(email)?.trim().orEmpty()
            val code = codeStr.toIntOrNull() ?: return@withContext StaticDataInfo.RESULT_CODE_ERR

            return@withContext when (code) {
                StaticDataInfo.RESULT_CODE_200,
                StaticDataInfo.RESULT_NO_USER,
                StaticDataInfo.RESULT_PWD_ERR,
                StaticDataInfo.RESULT_MEMBER_CODE_ERR,
                StaticDataInfo.RESULT_NO_DATA -> code
                else -> StaticDataInfo.RESULT_CODE_ERR
            }
        } catch (e: Exception) {
            Log.e("FindPassword", "Exception during find(): ${e.message}", e)
            StaticDataInfo.RESULT_CODE_ERR
        }
    }

    private fun saveTempPassword(tempPassword: String) {
        context.getSharedPreferences("TempPasswordInfo", Context.MODE_PRIVATE).edit().apply {
            putString("TempPassword", tempPassword)
            apply()
        }
    }
}
