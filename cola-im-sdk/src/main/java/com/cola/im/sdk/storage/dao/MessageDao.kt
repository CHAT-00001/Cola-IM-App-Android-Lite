package com.cola.im.sdk.storage.dao

import androidx.room.*
import com.cola.im.sdk.core.state.MessageStatus
import com.cola.im.sdk.storage.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 消息 DAO——所有数据库操作使用 @Transaction 事务批量处理
 */
@Dao
interface MessageDao {

    /**
     * 插入单条消息（幂等：重复 send_id + from_uid 忽略）
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: MessageEntity)

    /**
     * 批量插入消息（事务批量，禁止单条循环）
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Transaction
    suspend fun insertMessages(messages: List<MessageEntity>)

    /**
     * 按时间倒序观察消息列表
     */
    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun observeMessages(limit: Int, offset: Int): Flow<List<MessageEntity>>

    /**
     * 更新消息状态
     */
    @Query("UPDATE messages SET status = :status WHERE send_id = :sendId")
    suspend fun updateStatus(sendId: String, status: MessageStatus)

    /**
     * 收到 Server_ACK 后补全 sync_id、seq_no、sync_at，更新状态
     */
    @Query("""
        UPDATE messages 
        SET sync_id = :syncId, seq_no = :seqNo, sync_at = :syncAt, status = :status
        WHERE send_id = :sendId
    """)
    suspend fun updateServerAck(
        sendId: String,
        syncId: String,
        seqNo: Long,
        syncAt: Long,
        status: MessageStatus
    )

    /**
     * 批量标记已读
     */
    @Query("UPDATE messages SET status = 'READ' WHERE send_id IN (:msgIds)")
    suspend fun markAsRead(msgIds: List<String>)

    /**
     * 断线空洞检测：获取本地最大 seq_no
     */
    @Query("SELECT COALESCE(MAX(seq_no), 0) FROM messages")
    suspend fun getMaxSeqNo(): Long

    /**
     * 按 sync_id 查重（下行消息去重）
     */
    @Query("SELECT COUNT(*) FROM messages WHERE sync_id = :syncId")
    suspend fun countBySyncId(syncId: String): Int

    /**
     * 获取 SENDING 状态的消息（断线重发用）
     */
    @Query("SELECT * FROM messages WHERE status = 'SENDING' ORDER BY timestamp ASC")
    suspend fun getPendingMessages(): List<MessageEntity>

    /**
     * 删除所有消息
     */
    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}