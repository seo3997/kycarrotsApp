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

    /** 마지막으로 서버에 저장된 FCM 토큰 */
    private fun getLastToken(context: Context): String {
        return context.getSharedPreferences(PUSH_PREF, Context.MODE_PRIVATE)
            .getString(KEY_LAST_FCM_TOKEN, "").orEmpty()
    }

    /** 마지막 FCM 토큰 로컬 저장 */
    private fun saveLastToken(context: Context, token: String) {
        context.getSharedPreferences(PUSH_PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_FCM_TOKEN, token)
            .apply()
    }

    /** (선택) 로그아웃 시 토큰 정보 제거 */
    fun clearLastToken(context: Context) {
        context.getSharedPreferences(PUSH_PREF, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_FCM_TOKEN)
            .apply()
    }
    /**
     * ✅ 정책: 로컬(lastToken)이 비어있을 때만 현재 FCM 토큰 조회 → 서버 저장 시도
     * - 앱 시작 / 로그인 성공 시 1회 호출 추천
     */
    fun ensureTokenRegistered(context: Context) {
        val last = getLastToken(context)
        if (last.isNotEmpty()) {
            Log.d("FCM", "✅ last token exists → skip getToken")
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (token.isNullOrEmpty()) return@addOnSuccessListener
                Log.d("FCM", "🔄 ensureTokenRegistered token=$token")
                sendTokenToServer(context, token) // 내부에서 중복/로그인 체크/저장까지 함
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "❌ getToken failed", e)
            }
    }
    /**
     * ✅ 서버에 푸시 토큰 저장 (중복 방지 포함)
     * - 이전에 저장한 토큰과 같으면 서버 호출 안 함
     * - 로그인 전이면 스킵
     * - 서버 저장 성공 시에만 로컬에 토큰 저장
     */
    fun sendTokenToServer(context: Context, token: String) {

        val lastToken = getLastToken(context)
        if (lastToken == token) {
            Log.d("FCM", "⏭️ same token → server upload skip")
            return
        }

        val prefs = context.getSharedPreferences(LOGIN_PREF, Context.MODE_PRIVATE)
        val userId = prefs.getString(KEY_USER_ID, "").orEmpty()
        val userNo = prefs.getString(KEY_USER_NO, "").orEmpty()

        if (userId.isEmpty()) {
            Log.w("FCM", "userId 없음 → 로그인 후 토큰 전송 예정")
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
                    .registerPushToken(pushTokenVo)   // Boolean 반환 구조

                if (ok) {
                    saveLastToken(context, token)   // ✅ 성공 시만 로컬 저장
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
