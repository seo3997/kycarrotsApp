package com.whomade.kycarrots.loginout

import android.content.Context
import android.content.Intent
import com.whomade.kycarrots.loginout.LoginActivity

object AuthManager {

    fun logout(context: Context) {
        // 1. 로그인 정보 초기화
        context.getSharedPreferences("SaveLoginInfo", Context.MODE_PRIVATE).edit().apply {
            putString("LogIn_ID", "")
            putString("LogIn_PWD", "")
            putString("LogIn_MEMBERCODE", "")
            putBoolean("IsLogin", false)
            apply()
        }

        // 2. 토큰 정보 초기화
        context.getSharedPreferences("TokenInfo", Context.MODE_PRIVATE).edit().apply {
            putString("", "")
            apply()
        }

        // 3. LoginActivity로 이동 후 현재 액티비티 종료
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}
