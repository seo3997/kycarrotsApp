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
    public const val KEY_LOGIN_CD = "LogIn_CD"
    public const val KEY_SOCIAL_ID = "LogIn_SOCIAL_ID"
    public const val KEY_BRANCH_ID = "LogIn_BRANCH_ID"
    public const val KEY_BRANCH_NAME = "LogIn_BRANCH_NAME"
    public const val KEY_TOSS_CLIENT_KEY = "LogIn_TOSS_CLIENT_KEY"
    public const val KEY_BASE_SHIPPING_FEE = "LogIn_BASE_SHIPPING_FEE"
    public const val KEY_FREE_SHIPPING_THRESHOLD = "LogIn_FREE_SHIPPING_THRESHOLD"

    fun saveLoginInfo(context: Context, email: String, loginNo: String, password: String, memberCode: String, loginNm: String, loginCd: String, loginSocialId: String, branchId: String? = null, branchName: String? = null, tossClientKey: String? = null, baseShippingFee: Int = 0, freeShippingThreshold: Int = 0) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_ID, email)
            putString(KEY_NO, loginNo)
            putString(KEY_NM, loginNm)
            putString(KEY_PWD, password)
            putString(KEY_MEMBER_CODE, memberCode)
            putBoolean(KEY_IS_LOGIN, true)
            putString(KEY_LOGIN_CD, loginCd)
            putString(KEY_SOCIAL_ID, loginSocialId)
            putString(KEY_BRANCH_ID, branchId ?: "")
            putString(KEY_BRANCH_NAME, branchName ?: "")
            putString(KEY_TOSS_CLIENT_KEY, tossClientKey ?: "")
            putInt(KEY_BASE_SHIPPING_FEE, baseShippingFee)
            putInt(KEY_FREE_SHIPPING_THRESHOLD, freeShippingThreshold)
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

    /** 저장된 사용자 ID 가져오기 */
    fun getUserPassword(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PWD, "") ?: ""
    }

    fun getMemberCode(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MEMBER_CODE, "") ?: ""
    }

    fun getUserLoginCd(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOGIN_CD, "") ?: ""
    }

    fun getUserSocialId(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SOCIAL_ID, "") ?: ""
    }

    fun getBranchId(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BRANCH_ID, "") ?: ""
    }

    fun getBranchName(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_BRANCH_NAME, "") ?: ""
    }

    fun getTossClientKey(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOSS_CLIENT_KEY, "") ?: ""
    }

    fun getBaseShippingFee(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_BASE_SHIPPING_FEE, 0)
    }

    fun getFreeShippingThreshold(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_FREE_SHIPPING_THRESHOLD, 0)
    }


    fun isLoggedIn(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_IS_LOGIN, false)
    }


    fun clearLoginInfo(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
