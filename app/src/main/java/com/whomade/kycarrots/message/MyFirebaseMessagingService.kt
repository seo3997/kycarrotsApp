package com.whomade.kycarrots.message

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")

        // 알림(Notification) 영역 데이터
        val notification = remoteMessage.notification
        if (notification != null) {
            Log.d("FCM", "알림 제목: ${notification.title}")
            Log.d("FCM", "알림 내용: ${notification.body}")
        } else {
            Log.d("FCM", "알림 데이터 없음")
        }

        // 데이터 메시지 영역도 확인하고 싶으면 (커스텀 데이터)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "데이터 메시지: ${remoteMessage.data}")
        }
    }
}
