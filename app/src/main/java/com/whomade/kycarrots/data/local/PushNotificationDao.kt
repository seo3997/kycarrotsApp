// com.whomade.kycarrots.data.local.PushNotificationDao.kt
package com.whomade.kycarrots.data.local

import androidx.room.*

@Dao
interface PushNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PushNotificationEntity): Long

    @Query("""
        SELECT * FROM push_notification
        WHERE userId = :userId AND (:onlyUnread == 0 OR isRead = 0)
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun list(userId: String, onlyUnread: Int = 0, limit: Int = 50, offset: Int = 0): List<PushNotificationEntity>

    @Query("UPDATE push_notification SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long): Int

    @Query("UPDATE push_notification SET isRead = 1 WHERE userId = :userId AND isRead = 0")
    suspend fun markAllRead(userId: String): Int

    @Query("DELETE FROM push_notification WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("DELETE FROM push_notification WHERE userId = :userId")
    suspend fun deleteAll(userId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM push_notification WHERE userId = :userId AND isRead = 0)")
    suspend fun existsUnread(userId: String): Boolean

    @Query("SELECT COUNT(*) FROM push_notification WHERE userId = :userId AND isRead = 0")
    suspend fun countUnread(userId: String): Int
}
