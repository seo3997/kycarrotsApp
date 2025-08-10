// com.whomade.kycarrots.data.local.PushRepository.kt
package com.whomade.kycarrots.data.local

class PushRepository(private val dao: PushNotificationDao) {

    suspend fun save(item: PushNotificationEntity) = dao.insert(item)

    suspend fun list(userId: String, onlyUnread: Boolean, limit: Int, offset: Int) =
        dao.list(userId, if (onlyUnread) 1 else 0, limit, offset)

    suspend fun markRead(id: Long) = dao.markRead(id)
    suspend fun markAllRead(userId: String) = dao.markAllRead(userId)
    suspend fun delete(id: Long) = dao.delete(id)
    suspend fun deleteAll(userId: String) = dao.deleteAll(userId)
    suspend fun existsUnread(userId: String) = dao.existsUnread(userId)
    suspend fun countUnread(userId: String) = dao.countUnread(userId)
}
