package com.cola.im.sdk.storage

import android.content.Context
import com.cola.im.sdk.core.state.MessageStatus
import com.cola.im.sdk.storage.dao.MessageDao
import com.cola.im.sdk.storage.database.AppDatabase
import com.cola.im.sdk.storage.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 消息仓库——存储层唯一入口
 *
 * 职责：
 * - 乐观写入消息
 * - 暴露 Flow<List<MessageEntity>> 供 UI 订阅（SSOT）
 * - 处理 Server_ACK/Client_ACK 状态更新
 * - 断线空洞检测（getMaxSeqNo）
 */
class MessageRepository(
    context: Context,
    private val uid: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dao: MessageDao

    init {
        val db = AppDatabase.getInstance(context, uid)
        dao = db.messageDao()
    }

    /**
     * 乐观写入消息（生成 send_id，标记 SENDING）
     *
     * @return sendId
     */
    fun sendMessage(text: String, contentType: String = "text"): String {
        val sendId = UUID.randomUUID().toString()
        val entity = MessageEntity(
            sendId = sendId,
            fromUid = uid,
            content = text,
            contentType = contentType,
            status = MessageStatus.SENDING,
            timestamp = System.currentTimeMillis()
        )
        scope.launch {
            dao.insertMessage(entity)
            Timber.d("Message optimistic write: sendId=$sendId")
        }
        return sendId
    }

    /**
     * 插入消息实体（下行消息接收用）
     */
    fun insertMessage(entity: MessageEntity) {
        scope.launch {
            dao.insertMessage(entity)
        }
    }

    /**
     * 批量插入消息（事务批量）
     */
    fun insertMessages(entities: List<MessageEntity>) {
        scope.launch {
            dao.insertMessages(entities)
        }
    }

    /**
     * 观察消息列表（Single Source of Truth）
     */
    fun observeMessages(limit: Int = 50, offset: Int = 0): Flow<List<MessageEntity>> {
        return dao.observeMessages(limit, offset)
    }

    /**
     * 更新消息状态
     */
    fun updateStatus(sendId: String, status: MessageStatus) {
        scope.launch {
            dao.updateStatus(sendId, status)
        }
    }

    /**
     * 收到 Server_ACK 后更新
     */
    fun updateServerAck(
        sendId: String,
        syncId: String,
        seqNo: Long,
        syncAt: Long,
        status: MessageStatus
    ) {
        scope.launch {
            dao.updateServerAck(sendId, syncId, seqNo, syncAt, status)
        }
    }

    /**
     * 批量标记已读
     */
    fun markAsRead(msgIds: List<String>) {
        scope.launch {
            dao.markAsRead(msgIds)
        }
    }

    /**
     * 获取本地最大 seq_no（断线空洞检测）
     */
    suspend fun getMaxSeqNo(): Long {
        return dao.getMaxSeqNo()
    }

    /**
     * 按 sync_id 查重
     */
    suspend fun existsBySyncId(syncId: String): Boolean {
        return dao.countBySyncId(syncId) > 0
    }

    /**
     * 获取待发送消息（断线重发用）
     */
    suspend fun getPendingMessages(): List<MessageEntity> {
        return dao.getPendingMessages()
    }

    /**
     * 释放资源
     */
    fun close() {
        AppDatabase.closeDatabase(uid)
        Timber.i("MessageRepository closed (uid=$uid)")
    }
}