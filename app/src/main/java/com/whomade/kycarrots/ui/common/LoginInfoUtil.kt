package com.whomade.kycarrots.ui.common

import android.content.Context

object LoginInfoUtil {
    public const val PREF_NAME = "SaveLoginInfo"

    public const val KEY_ID = "LogIn_ID"
    public const val KEY_NO = "LogIn_NO"
    public const val KEY_NM = "LogIn_NM"
    public const val KEY_PWD = "LogIn_PWD"
    public const val KEY_MEMBER_CODE = "LogIn_MEMBERCODE"
    public const val KEY_IS_LOGIN = "IsLogin"

    fun saveLoginInfo(context: Context, email: String, loginNo: String, password: String, memberCode: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_NO, loginNo)
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

    /** 저장된 사용자 ID 가져오기 */
    fun getUserNo(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NO, "") ?: ""
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
