package com.whomade.kycarrots.ui.common

import android.content.Context

object TokenUtil {
    private const val PREF_NAME = "TokenInfo"
    private const val TOKEN_KEY = "token"

    fun getToken(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(TOKEN_KEY, "") ?: ""
    }

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    fun clearToken(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(TOKEN_KEY)
            .apply()
    }
}
