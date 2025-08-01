package com.whomade.kycarrots.message

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.whomade.kycarrots.IntroActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.PushTokenVo
import com.whomade.kycarrots.domain.service.AppServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // 서버에 토큰 전송
        PushTokenUtil.sendTokenToServer(applicationContext, token)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title ?: "새 알림"
        val body = remoteMessage.notification?.body ?: "알림 내용 없음"

        val data = remoteMessage.data
        val type = data["type"] ?: "default"

        Log.d("FCM", "type: $type")

        when (type) {
            "chat" -> {
                val roomId = data["roomId"]
                val buyerId = data["buyerId"]
                val sellerId = data["sellerId"]
                val productId = data["productId"]
                val msg = data["msg"]

                Log.d("FCM", "chat → roomId: $roomId, buyerId: $buyerId, sellerId: $sellerId, productId: $productId, msg: $msg")
                showNotification(title, body, roomId, buyerId, sellerId, productId, type, msg)
            }

            "product" -> {
                val productId = data["productId"]
                val userId = data["userId"]
                Log.d("FCM", "product → productId: $productId userId: $userId")
                showNotification(title, body, null, null, userId, productId, type, null)
            }

            else -> {
                // 기타 유형 처리 (optional)
                Log.w("FCM", "Unknown or missing type")
                showNotification(title, body, null, null, null, null, type, null)
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        roomId: String?,
        buyerId: String?,
        sellerId: String?,
        productId: String?,
        type: String?,
        msg: String?
    ) {
        // ① 알림 채널 ID 및 정보 분기
        val channelId = when (type) {
            "chat" -> "chat_channel"
            "product" -> "product_channel"
            else -> "default_channel"
        }

        val (channelName, channelDescription) = when (type) {
            "chat" -> "채팅 알림" to "채팅 관련 알림입니다."
            "product" -> "상품 알림" to "신규 상품 관련 알림입니다."
            else -> "일반 알림" to "기타 알림입니다."
        }

        // ② 알림 채널 생성 (O 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // ③ Intent: 항상 IntroActivity → IntroActivity 내부에서 type으로 분기
        val intent = Intent(this, IntroActivity::class.java).apply {
            putExtra("type", type)
            putExtra("roomId", roomId)
            putExtra("buyerId", buyerId)
            putExtra("sellerId", sellerId)
            putExtra("productId", productId)
            putExtra("msg", msg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ④ 알림 생성 및 표시
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.android_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
            } else {
                Log.d("FCM", "알림 권한이 없습니다. 알림을 띄우지 않습니다.")
            }
        } else {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

}
