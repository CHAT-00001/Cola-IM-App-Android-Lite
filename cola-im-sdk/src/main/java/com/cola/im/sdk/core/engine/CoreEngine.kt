package com.cola.im.sdk.core.engine

import com.cola.im.sdk.core.state.ConnectionState
import com.cola.im.sdk.core.state.MessageStatus
import com.cola.im.sdk.network.ConnectionManager
import com.cola.im.sdk.storage.MessageRepository
import com.cola.im.sdk.storage.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * MVI 核心引擎——消息状态机控制中心
 *
 * 数据流向：
 * UI 触发 Intent → CoreEngine 处理 → 更新 Storage → Storage 驱动 Flow 生成新 State → UI 自动重绘
 */
class CoreEngine(
    private val messageRepository: MessageRepository,
    private val connectionManager: ConnectionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ===== Intent =====
    sealed class Intent {
        data class SendMessage(val text: String, val contentType: String = "text") : Intent()
        data class MarkAsRead(val msgIds: List<String>) : Intent()
        data class ServerAckReceived(
            val sendId: String,
            val syncId: String,
            val seqNo: Long,
            val syncAt: Long
        ) : Intent()

        data class ClientAckReceived(val syncId: String) : Intent()
        object Reconnect : Intent()
    }

    // ===== State =====
    data class SendResult(
        val sendId: String,
        val status: MessageStatus
    )

    private val _sendResult = MutableStateFlow<SendResult?>(null)
    val sendResult: StateFlow<SendResult?> = _sendResult.asStateFlow()

    /**
     * 处理 Intent——MVI 核心方法
     */
    fun process(intent: Intent) {
        when (intent) {
            is Intent.SendMessage -> handleSendMessage(intent)
            is Intent.MarkAsRead -> handleMarkAsRead(intent)
            is Intent.ServerAckReceived -> handleServerAck(intent)
            is Intent.ClientAckReceived -> handleClientAck(intent)
            is Intent.Reconnect -> handleReconnect()
        }
    }

    /**
     * 观察消息列表（SSOT——Single Source of Truth）
     */
    fun observeMessages(limit: Int = 50, offset: Int = 0): Flow<List<MessageEntity>> {
        return messageRepository.observeMessages(limit, offset)
    }

    // ===== Intent 处理 =====

    /**
     * 1. 乐观写入：生成 UUID send_id，落库标记 SENDING，通过 Flow 驱动 UI
     * 2. 通过 ConnectionManager 发送
     */
    private fun handleSendMessage(intent: Intent.SendMessage) {
        scope.launch {
            val sendId = UUID.randomUUID().toString()
            val entity = MessageEntity(
                sendId = sendId,
                content = intent.text,
                contentType = intent.contentType,
                status = MessageStatus.SENDING,
                timestamp = System.currentTimeMillis()
            )

            // 乐观写入
            messageRepository.insertMessage(entity)
            _sendResult.value = SendResult(sendId, MessageStatus.SENDING)

            // 通过连接管理器发送
            val success = connectionManager.sendMessage(entity)
            if (!success) {
                // 加入断线重发队列（2000ms 超时）
                messageRepository.updateStatus(sendId, MessageStatus.FAILED)
                _sendResult.value = SendResult(sendId, MessageStatus.FAILED)
            }
        }
    }

    private fun handleMarkAsRead(intent: Intent.MarkAsRead) {
        scope.launch {
            messageRepository.markAsRead(intent.msgIds)
            connectionManager.reportReadAck(intent.msgIds)
        }
    }

    /**
     * 3&4. 收到 Server_ACK → 更新状态为 SUCCESS
     */
    private fun handleServerAck(intent: Intent.ServerAckReceived) {
        scope.launch {
            messageRepository.updateServerAck(
                sendId = intent.sendId,
                syncId = intent.syncId,
                seqNo = intent.seqNo,
                syncAt = intent.syncAt,
                status = MessageStatus.SUCCESS
            )
            _sendResult.value = SendResult(intent.sendId, MessageStatus.SUCCESS)
        }
    }

    /**
     * 收到 Client_ACK → 幂等去重处理
     */
    private fun handleClientAck(intent: Intent.ClientAckReceived) {
        // 下行消息已在 Repository 层做 sync_id 去重
        // 此处由 ConnectionManager 回调驱动
        Timber.d("Client_ACK received for sync_id=${intent.syncId}")
    }

    private fun handleReconnect() {
        connectionManager.connect()
    }
}