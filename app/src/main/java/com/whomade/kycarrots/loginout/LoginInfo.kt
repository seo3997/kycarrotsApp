package com.whomade.kycarrots.loginout

import android.content.Context
import android.content.Intent
import android.util.Log
import com.whomade.kycarrots.CheckLoginService
import com.whomade.kycarrots.StaticDataInfo
import com.whomade.kycarrots.data.model.LoginResponse
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TokenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginInfo(
    private val context: Context,
    private val email: String,
    private val password: String,
    private val loginCd: String,
    private val appVersion: String,
    private val providerUserId: String,
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

            val regId = ""
            val response = appService.login(email, password,loginCd, regId, appVersion,providerUserId)
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
        LoginInfoUtil.saveLoginInfo(context,email,info.login_idx?: "", password,info.member_code?: "",info.login_nm?: "",loginCd,providerUserId)
        TokenUtil.saveToken(context,info.token ?: "")
    }
}
