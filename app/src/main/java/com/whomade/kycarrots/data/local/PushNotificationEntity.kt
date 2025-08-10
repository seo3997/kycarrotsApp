// com.whomade.kycarrots.data.local.PushNotificationEntity.kt
package com.whomade.kycarrots.data.local

import androidx.room.*

@Entity(
    tableName = "push_notification",
    indices = [Index(value = ["userId", "createdAt"])]
)
data class PushNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,                 // 수신자(내 계정 ID)
    val type: String,                   // "CHAT", "PRODUCT_REGISTERED", "PRODUCT_APPROVED", ...
    val title: String,
    val body: String?,
    val productId: Long?,               // 상품 관련이면 사용
    val sellerId: String?,              // ✅ 추가
    val roomId: String?,                // 채팅 관련이면 사용
    val deeplink: String?,              // app://product/123, app://chat/room/abc ...
    val isRead: Boolean = false,        // 미리 “읽음/안읽음” 표시용 (숫자 카운트는 관리 안 함)
    val createdAt: Long = System.currentTimeMillis()
)
