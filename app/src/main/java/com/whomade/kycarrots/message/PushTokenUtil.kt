package com.whomade.kycarrots.message

import android.content.Context
import android.util.Log
import com.whomade.kycarrots.data.model.PushTokenVo
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushTokenUtil {

    fun sendTokenToServer(context: Context, token: String) {
        val prefs = context.getSharedPreferences("SaveLoginInfo", Context.MODE_PRIVATE)
        val sUID = prefs.getString("LogIn_ID", "").orEmpty()
        val sUNo = prefs.getString("LogIn_NO", "").orEmpty()

        if (sUID.isNotEmpty()) {
            val pushTokenVo = PushTokenVo(userNo = sUNo, userId = sUID, pushToken = token)

            CoroutineScope(Dispatchers.IO).launch {
                val result = AppServiceProvider.getService().registerPushToken(pushTokenVo)
                Log.d("FCM", "Push 토큰 서버 등록: ${if (result) "성공" else "실패"}")
            }
        } else {
            Log.w("FCM", "userId 없음. 토큰 서버 전송 생략")
        }
    }
}
