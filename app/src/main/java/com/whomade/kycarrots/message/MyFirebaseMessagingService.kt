package com.whomade.kycarrots.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.whomade.kycarrots.R

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 로그 출력 (기존 코드)
        Log.d("FCM", "From: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title ?: "새 알림"
        val body = remoteMessage.notification?.body ?: "알림 내용 없음"

        Log.d("FCM", "title: ${title}")
        Log.d("body", "title: ${body}")

        // 알림 직접 띄우기
        showNotification(title, body)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "데이터 메시지: ${remoteMessage.data}")

            // 예시: roomId, type, msg 추출
            val roomId = remoteMessage.data["roomId"]
            val type = remoteMessage.data["type"]
            val msg = remoteMessage.data["msg"]

            Log.d("FCM", "roomId: $roomId, type: $type, msg: $msg")

            // 필요하다면 데이터 활용 (예: 채팅방 바로 이동 등)
            // if (type == "chat") { ... }
        }

        // 커스텀 데이터 로그 (필요 시)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "데이터 메시지: ${remoteMessage.data}")
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "chat_channel"
        // 알림 채널 생성 (Android 8.0 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "채팅 알림"
            val descriptionText = "채팅 관련 알림입니다."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 빌드
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.android_icon) // mipmap 아이콘 사용!
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Android 13 이상은 권한 체크 필요!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Service에서는 checkSelfPermission 사용
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
            } else {
                // 권한 없으면 알림 띄우지 않고 로그만 출력
                Log.d("FCM", "알림 권한이 없습니다. 알림을 띄우지 않습니다.")
            }
        } else {
            // Android 12 이하: 바로 알림 띄우기
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

}