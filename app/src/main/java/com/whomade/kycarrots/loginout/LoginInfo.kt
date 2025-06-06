package com.whomade.kycarrots.loginout

import android.content.Context
import android.content.Intent
import android.util.Log
import com.whomade.kycarrots.CheckLoginService
import com.whomade.kycarrots.StaticDataInfo
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.domain.service.AppService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginInfo(
    private val context: Context,
    private val email: String,
    private val password: String,
    private val appVersion: String,
    private val userType: String,
    private val appService: AppService
) {

    suspend fun login(): Int = withContext(Dispatchers.IO) {
        try {
            if (!CheckLoginService.start_service) {
                val intent = Intent(CheckLoginService::class.java.name).apply {
                    setPackage(context.packageName)
                }
                context.startService(intent)
            }

            val regId = context.getSharedPreferences("SaveRegId", Context.MODE_PRIVATE)
                .getString("setRegId", "1") ?: "1"

            val response = appService.login(email, password, regId, appVersion)
            if (response != null && response.resultCode == StaticDataInfo.RESULT_CODE_200) {
                saveLoginInfo(response)
                return@withContext StaticDataInfo.RESULT_CODE_200
            } else {
                return@withContext response?.resultCode ?: StaticDataInfo.RESULT_CODE_ERR
            }


        } catch (e: Exception) {
            Log.e("LoginInfo", "Login Exception: ${e.message}", e)
            return@withContext StaticDataInfo.RESULT_CODE_ERR
        }
    }

    private fun saveLoginInfo(info: LoginResponse) {
        context.getSharedPreferences("SaveLoginInfo", Context.MODE_PRIVATE).edit().apply {
            putString("LogIn_ID", email)
            putString("LogIn_PWD", password)
            putString("LogIn_USERTYPE", userType)
            putBoolean("IsLogin", true)
            apply()
        }

        context.getSharedPreferences("TokenInfo", Context.MODE_PRIVATE).edit().apply {
            putString("token", info.token ?: "")
            apply()
        }
    }
}
