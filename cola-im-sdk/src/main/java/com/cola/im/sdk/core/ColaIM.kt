package com.cola.im.sdk.core

import android.content.Context
import com.cola.im.sdk.core.state.ConnectionState
import com.cola.im.sdk.network.ConnectionManager
import com.cola.im.sdk.storage.MessageRepository
import timber.log.Timber
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cola-IM SDK 主入口 Facade 门面
 *
 * 侧载模式（Sideload Mode）：SDK 以独立库模块嵌入宿主 APP，
 * 不依赖任何业务框架，仅通过 MVI 核心引擎驱动消息流转。
 *
 * 使用示例：
 * ```kotlin
 * // 初始化
 * ColaIM.init(context, "user_uid_123")
 *
 * // 发送消息
 * ColaIM.sendMessage("hello")
 *
 * // 观察消息列表
 * ColaIM.observeMessages().collect { messages -> ... }
 * ```
 */
object ColaIM {

    private var initialized = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private lateinit var messageRepository: MessageRepository
    private lateinit var connectionManager: ConnectionManager

    /**
     * 初始化 Cola-IM SDK（侧载模式）
     *
     * @param context Application Context
     * @param uid 当前登录用户 ID
     * @param serverHost 服务端地址（默认从 config 读取）
     */
    fun init(context: Context, uid: String, serverHost: String = "https://api2.damawei.com") {
        if (initialized) return

        messageRepository = MessageRepository(context, uid)
        connectionManager = ConnectionManager(uid, serverHost, messageRepository, _connectionState)

        // 启动自动连接
        connectionManager.connect()

        initialized = true
        Timber.i("Cola-IM SDK initialized (uid=$uid, sideload mode)")
    }

    /**
     * 发送消息（UI → Intent → Core → Storage → Flow）
     *
     * @param text 消息文本
     * @param contentType 消息类型（默认 text）
     */
    fun sendMessage(text: String, contentType: String = "text"): String {
        checkInitialized()
        return messageRepository.sendMessage(text, contentType)
    }

    /**
     * 观察消息列表（Single Source of Truth）
     *
     * @param limit 每次加载条数
     * @param offset 偏移量
     */
    fun observeMessages(limit: Int = 50, offset: Int = 0) =
        messageRepository.observeMessages(limit, offset)

    /**
     * 标记消息已读（Read ACK）
     *
     * @param msgIds 已读消息 ID 列表
     */
    fun markAsRead(msgIds: List<String>) {
        checkInitialized()
        messageRepository.markAsRead(msgIds)
    }

    /**
     * 断开连接并释放资源
     */
    fun shutdown() {
        if (!initialized) return
        connectionManager.disconnect()
        messageRepository.close()
        scope.cancel()
        initialized = false
        Timber.i("Cola-IM SDK shutdown")
    }

    private fun checkInitialized() {
        require(initialized) { "Cola-IM SDK not initialized. Call ColaIM.init() first." }
    }
}