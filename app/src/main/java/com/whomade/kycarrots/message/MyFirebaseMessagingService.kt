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
import com.whomade.kycarrots.data.local.NotifType
import com.whomade.kycarrots.data.local.PushNotificationEntity
import com.whomade.kycarrots.data.local.PushRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        PushTokenUtil.sendTokenToServer(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")

        val data  = remoteMessage.data
        val type  = data["type"] ?: "default"
        val title = data["title"] ?: "새 알림"
        val body  = data["body"]  ?: "알림 내용 없음"

        when (type) {
            "chat" -> {
                val roomId    = data["roomId"]
                val productId = data["productId"]
                val msg       = data["msg"]

                // ✅ 로컬 DB 저장: targetId로 roomId 사용
                savePushLocally(
                    type = NotifType.CHAT,
                    title = title,
                    body = body,
                    targetId = roomId,
                    deeplink = "app://chat/room/${roomId ?: ""}"
                )

                showNotification(title, body, roomId, productId, null, type, msg)
            }
            "product" -> {
                val productId = data["productId"]

                // ✅ 로컬 DB 저장: targetId로 productId 사용
                savePushLocally(
                    type = NotifType.PRODUCT,
                    title = title,
                    body = body,
                    targetId = productId,
                    deeplink = "app://product/${productId ?: ""}"
                )

                showNotification(title, body, null, productId, null, type, null)
            }
            "order" -> {
                val orderId = data["order_id"]
                // ✅ 로컬 DB 저장: targetId로 orderId 사용
                savePushLocally(
                    type = NotifType.ORDER,
                    title = title,
                    body = body,
                    targetId = orderId,
                    deeplink = "app://order/${orderId ?: ""}"
                )
                showNotification(title, body, null, null, orderId, type, null)
            }
            else -> {
                // 기타 유형
                savePushLocally(
                    type = NotifType.SYS,
                    title = title,
                    body = body,
                    targetId = null,
                    deeplink = null
                )
                showNotification(title, body, null,  null, null, type, null)
            }
        }
    }

    // ✅ 푸시 수신 시 로컬(Room) 저장 함수 (targetId 통합 버전)
    private fun savePushLocally(
        type: String,
        title: String,
        body: String?,
        targetId: String?,
        deeplink: String?
    ) {
        val prefs  = applicationContext.getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val userId = prefs.getString("LogIn_ID", "") ?: ""
        if (userId.isEmpty()) {
            Log.w("FCM", "로컬저장 스킵: 로그인 사용자 없음")
            return
        }

        val entity = PushNotificationEntity(
            userId   = userId,
            type     = type,
            title    = title,
            body     = body,
            targetId = targetId,
            deeplink = deeplink,
            isRead   = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                PushRepositoryProvider.get(applicationContext).save(entity)
                Log.d("FCM", "로컬 DB 저장 완료: $type / $title")
            } catch (e: Exception) {
                Log.e("FCM", "로컬 DB 저장 실패", e)
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        roomId: String?,
        productId: String?,
        orderId: String?,
        type: String?,
        msg: String?
    ) {
        val channelId = when (type) {
            "chat" -> "chat_channel"
            "product" -> "product_channel"
            "order" -> "order_channel"
            else -> "default_channel"
        }

        val (channelName, channelDescription) = when (type) {
            "chat" -> "채팅 알림" to "채팅 관련 알림입니다."
            "product" -> "상품 알림" to "신규 상품 관련 알림입니다."
            "order" -> "주문 알림" to "주문 관련 알림입니다."
            else -> "일반 알림" to "기타 알림입니다."
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, IntroActivity::class.java).apply {
            putExtra("type", type)
            putExtra("roomId", roomId)
            putExtra("productId", productId)
            putExtra("order_id", orderId)
            putExtra("msg", msg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
                Log.d("FCM", "알림 권한 없음 → 알림 표시 생략")
            }
        } else {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
