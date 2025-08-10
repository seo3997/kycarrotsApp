package com.whomade.kycarrots.ui.common

import android.content.Context

object LoginInfoUtil {
    private const val PREF_NAME = "SaveLoginInfo"

    private const val KEY_ID = "LogIn_ID"
    private const val KEY_PWD = "LogIn_PWD"
    private const val KEY_MEMBER_CODE = "LogIn_MEMBERCODE"
    private const val KEY_IS_LOGIN = "IsLogin"

    fun saveLoginInfo(context: Context, email: String, password: String, memberCode: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_ID, email)
            putString(KEY_PWD, password)
            putString(KEY_MEMBER_CODE, memberCode)
            putBoolean(KEY_IS_LOGIN, true)
            apply()
        }
    }

    /** 저장된 사용자 ID 가져오기 */
    fun getUserId(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ID, "") ?: ""
    }

    fun getMemberCode(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MEMBER_CODE, "") ?: ""
    }

    fun isLoggedIn(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_LOGIN, false)
    }

    fun clearLoginInfo(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
