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

        Log.d("FCM", "title: ${title}")
        Log.d("body", "title: ${body}")

        // 데이터 payload
        val data = remoteMessage.data
        val roomId = data["roomId"]
        val buyerId = data["buyerId"]
        val sellerId = data["sellerId"]
        val productId = data["productId"]
        val type = data["type"]
        val msg = data["msg"]

        Log.d("FCM", "roomId: $roomId, buyerId: $buyerId, sellerId: $sellerId, productId: $productId, type: $type, msg: $msg")

        // 알림에 intent 포함
        showNotification(title, body, roomId, buyerId, sellerId, productId, type, msg)
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
        val channelId = "chat_channel"
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

        // Intent 준비 (IntroActivity → ChatActivity로 분기)
        val intent = Intent(this, IntroActivity::class.java).apply {
            putExtra("roomId", roomId)
            putExtra("buyerId", buyerId)
            putExtra("sellerId", sellerId)
            putExtra("productId", productId)
            putExtra("type", type)
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
            .setAutoCancel(true) // 클릭 시 알림 제거

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
