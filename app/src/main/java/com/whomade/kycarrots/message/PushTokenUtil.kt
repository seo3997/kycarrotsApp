package com.whomade.kycarrots.message

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.whomade.kycarrots.data.model.PushTokenVo
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushTokenUtil {

    private const val LOGIN_PREF = "SaveLoginInfo"
    private const val KEY_USER_ID = "LogIn_ID"
    private const val KEY_USER_NO = "LogIn_NO"

    // ✅ 푸시 토큰 전용 SharedPreferences
    private const val PUSH_PREF = "PushInfo"
    private const val KEY_LAST_FCM_TOKEN = "last_fcm_token"
    private const val KEY_LAST_USER_ID = "last_user_id"

    /** 마지막으로 서버에 저장된 FCM 토큰 */
    private fun getLastToken(context: Context): String {
        return context.getSharedPreferences(PUSH_PREF, Context.MODE_PRIVATE)
            .getString(KEY_LAST_FCM_TOKEN, "").orEmpty()
    }

    private fun getLastSavedUserId(context: Context): String {
        return context.getSharedPreferences(PUSH_PREF, Context.MODE_PRIVATE)
            .getString(KEY_LAST_USER_ID, "").orEmpty()
    }

    /** 마지막 FCM 토큰 및 유저 캐싱 */
    private fun saveLastTokenAndUser(context: Context, token: String, userId: String) {
        context.getSharedPreferences(PUSH_PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_FCM_TOKEN, token)
            .putString(KEY_LAST_USER_ID, userId)
            .apply()
    }

    /** (선택) 로그아웃 시 토큰 정보 제거 */
    fun clearLastToken(context: Context) {
        context.getSharedPreferences(PUSH_PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_FCM_TOKEN)
            .remove(KEY_LAST_USER_ID)
            .apply()
    }

    /**
     * ✅ 현재 FCM 토큰 조회 → 서버 저장 시도
     */
    fun ensureTokenRegistered(context: Context) {
        val prefs = context.getSharedPreferences(LOGIN_PREF, Context.MODE_PRIVATE)
        val currentUserId = prefs.getString(KEY_USER_ID, "").orEmpty()

        val lastToken = getLastToken(context)
        val lastUserId = getLastSavedUserId(context)

        // 토큰이 있고, 로그인된 아이디가 마지막으로 전송된 아이디와 같으면 스킵
        if (lastToken.isNotEmpty() && lastUserId == currentUserId && currentUserId.isNotEmpty()) {
            Log.d("FCM", "✅ token and user match → skip getToken")
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (token.isNullOrEmpty()) return@addOnSuccessListener
                Log.d("FCM", "🔄 ensureTokenRegistered token=\$token")
                sendTokenToServer(context, token)
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "❌ getToken failed", e)
            }
    }

    /**
     * ✅ 서버에 푸시 토큰 저장 (중복 방지 포함)
     * - 로그인 전이면 스킵
     * - 서버 저장 성공 시에만 로컬에 토큰 및 아이디 저장
     */
    fun sendTokenToServer(context: Context, token: String) {

        val prefs = context.getSharedPreferences(LOGIN_PREF, Context.MODE_PRIVATE)
        val userId = prefs.getString(KEY_USER_ID, "").orEmpty()
        val userNo = prefs.getString(KEY_USER_NO, "").orEmpty()

        if (userId.isEmpty()) {
            Log.w("FCM", "userId 없음 → 로그인 후 토큰 전송 예정")
            return
        }

        val lastToken = getLastToken(context)
        val lastUserId = getLastSavedUserId(context)

        if (lastToken == token && lastUserId == userId) {
            Log.d("FCM", "⏭️ same token & user → server upload skip")
            return
        }

        val pushTokenVo = PushTokenVo(
            userNo = userNo,
            userId = userId,
            pushToken = token,
            deviceType = "ANDROID"
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ok = AppServiceProvider
                    .getService()
                    .registerPushToken(pushTokenVo)

                if (ok) {
                    saveLastTokenAndUser(context, token, userId)
                    Log.d("FCM", "✅ Push 토큰 서버 저장 성공")
                } else {
                    Log.e("FCM", "❌ Push 토큰 서버 저장 실패")
                }
            } catch (e: Exception) {
                Log.e("FCM", "❌ Push 토큰 서버 저장 예외", e)
            }
        }
    }
}
