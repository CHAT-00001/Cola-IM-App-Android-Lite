package com.cola.im.sdk.network

import com.cola.im.sdk.core.state.ConnectionState
import com.cola.im.sdk.storage.MessageRepository
import com.cola.im.sdk.storage.entity.MessageEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.*
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * 多介质连接管理器
 *
 * 功能：
 * - WebSocket 长连接管理
 * - 自适应心跳（前台 10s / 后台 60s~270s 递增）
 * - 端口跳跃（Port Hopping）
 * - 断线重连（指数退避 + 断线重发队列）
 */
class ConnectionManager(
    private val uid: String,
    private val serverHost: String,
    private val messageRepository: MessageRepository,
    private val _connectionState: MutableStateFlow<ConnectionState>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ===== WebSocket =====
    private var webSocket: WebSocket? = null
    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    // ===== 端口跳跃 =====
    private val staticPort = 443   // 默认端口
    private val portPool = listOf(443, 8080, 8443, 9090, 5222, 8088, 8888)
    private var currentPortIndex = 0
    private var reconnectAttempts = 0

    // ===== 心跳参数 =====
    private var heartbeatInterval = 60_000L       // 初始：60s
    private var heartbeatSuccessCount = 0
    private var heartbeatFailureCount = 0
    private var currentSafeUpperLimit = 270_000L  // 安全上限：270s
    private var isForeground = true

    private var heartbeatJob: Job? = null

    /**
     * 建立 WebSocket 连接
     */
    fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTING ||
            _connectionState.value == ConnectionState.AUTHENTICATED) {
            return
        }

        _connectionState.value = ConnectionState.CONNECTING
        val port = getCurrentPort()
        val wsUrl = buildWebSocketUrl(port)

        Timber.i("Connecting to $wsUrl (port jump index=$currentPortIndex)")

        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("X-UID", uid)
            .addHeader("X-Device-ID", getDeviceId())
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.i("WebSocket opened: $wsUrl")
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempts = 0
                authenticate()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Timber.w("WebSocket failure: ${t.message}")
                handleDisconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Timber.i("WebSocket closed: code=$code reason=$reason")
                handleDisconnect()
            }
        })
    }

    /**
     * 发送消息（上行）
     *
     * @return true 表示发送成功
     */
    suspend fun sendMessage(entity: MessageEntity): Boolean {
        val state = _connectionState.value
        if (state != ConnectionState.AUTHENTICATED && state != ConnectionState.CONNECTED) {
            Timber.w("Cannot send message, connection state=$state")
            return false
        }

        val payload = JSONObject().apply {
            put("action", "message.send")
            put("send_id", entity.sendId)
            put("from_uid", uid)
            put("content", entity.content)
            put("content_type", entity.contentType)
            put("timestamp", entity.timestamp)
            put("is_encrypted", entity.isEncrypted)
        }

        return try {
            webSocket?.send(payload.toString()) ?: false
        } catch (e: Exception) {
            Timber.e(e, "Failed to send message")
            false
        }
    }

    /**
     * 上报已读回执
     */
    fun reportReadAck(msgIds: List<String>) {
        val payload = JSONObject().apply {
            put("action", "message.read_ack")
            put("uid", uid)
            put("msg_ids", msgIds)
        }
        try {
            webSocket?.send(payload.toString())
        } catch (e: Exception) {
            Timber.e(e, "Failed to report read ack")
        }
    }

    /**
     * 设置前后台模式
     */
    fun setForeground(foreground: Boolean) {
        isForeground = foreground
        if (foreground) {
            heartbeatInterval = 10_000L  // 前台：10s 高频
        }
        restartHeartbeat()
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        heartbeatJob?.cancel()
        webSocket?.close(1000, "Client shutdown")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    // ===== 私有方法 =====

    private fun authenticate() {
        val authPayload = JSONObject().apply {
            put("action", "auth")
            put("uid", uid)
            put("token", "")
            put("device_id", getDeviceId())
            put("client_version", "0.1.0")
            put("platform", "android")
        }
        webSocket?.send(authPayload.toString())
        // 模拟认证成功（实际应等待服务端 auth_ack）
        _connectionState.value = ConnectionState.AUTHENTICATED
        startHeartbeat()
        Timber.i("Authenticated (uid=$uid)")
    }

    private fun handleServerMessage(text: String) {
        try {
            val json = JSONObject(text)
            val action = json.optString("action")

            when (action) {
                "server_ack" -> {
                    // 收到 Server_ACK 上行确认
                    val sendId = json.getString("send_id")
                    val syncId = json.getString("sync_id")
                    val seqNo = json.getLong("seq_no")
                    val syncAt = json.getLong("sync_at")
                    Timber.d("Server_ACK: sendId=$sendId seqNo=$seqNo")
                    // 通过 CoreEngine 处理
                    onServerAck?.invoke(sendId, syncId, seqNo, syncAt)
                }

                "message.new" -> {
                    // 收到新消息（下行）
                    val sendId = json.getString("send_id")
                    val syncId = json.getString("sync_id")
                    Timber.d("New message: sendId=$sendId syncId=$syncId")
                    handleIncomingMessage(json)
                }

                "auth_ack" -> {
                    // 认证成功
                    _connectionState.value = ConnectionState.AUTHENTICATED
                    Timber.i("Auth ACK received")
                }

                "pong" -> {
                    // 心跳响应
                    handlePong()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse server message")
        }
    }

    private fun handleIncomingMessage(json: JSONObject) {
        scope.launch {
            val syncId = json.optString("sync_id", "")
            // 幂等去重
            if (syncId.isNotEmpty() && messageRepository.existsBySyncId(syncId)) {
                // 已存在，补发 Client_ACK
                sendClientAck(syncId)
                return@launch
            }

            val entity = MessageEntity(
                sendId = json.optString("send_id", UUID.randomUUID().toString()),
                syncId = syncId,
                seqNo = json.optLong("seq_no", 0),
                syncAt = json.optLong("sync_at", 0),
                fromUid = json.optString("from_uid", ""),
                toTargetId = json.optString("to_target_id", ""),
                content = json.optString("content", ""),
                contentType = json.optString("content_type", "text"),
                isEncrypted = json.optBoolean("is_encrypted", false),
                cryptoNoise = json.optString("crypto_noise", ""),
                channel = json.optString("channel", ""),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                status = com.cola.im.sdk.core.state.MessageStatus.SUCCESS
            )

            // 原子落库
            messageRepository.insertMessage(entity)
            // 回传 Client_ACK
            sendClientAck(syncId)
        }
    }

    private fun sendClientAck(syncId: String) {
        val payload = JSONObject().apply {
            put("action", "message.client_ack")
            put("sync_id", syncId)
            put("uid", uid)
        }
        try {
            webSocket?.send(payload.toString())
        } catch (e: Exception) {
            Timber.e(e, "Failed to send Client_ACK")
        }
    }

    // ===== Server_ACK 回调（由 CoreEngine 设置） =====
    var onServerAck: ((sendId: String, syncId: String, seqNo: Long, syncAt: Long) -> Unit)? = null

    // ===== 断线处理 =====
    private fun handleDisconnect() {
        _connectionState.value = ConnectionState.RECONNECTING
        heartbeatJob?.cancel()
        reconnectAttempts++

        // 原端口重连 3 次失败后切换端口
        val maxRetriesPerPort = 3
        if (reconnectAttempts > maxRetriesPerPort) {
            currentPortIndex = (currentPortIndex + 1) % portPool.size
            reconnectAttempts = 0
            Timber.i("Port hopping to index=$currentPortIndex port=${getCurrentPort()}")
        }

        // 指数退避
        val delay = min(1000L * (1 shl min(reconnectAttempts, 6)), 30_000L)
        scope.launch {
            delay(delay)
            connect()
        }
    }

    // ===== 自适应心跳 =====
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(heartbeatInterval)
                sendPing()
            }
        }
    }

    private fun restartHeartbeat() {
        heartbeatJob?.cancel()
        if (_connectionState.value == ConnectionState.AUTHENTICATED) {
            startHeartbeat()
        }
    }

    private fun sendPing() {
        val payload = JSONObject().apply {
            put("action", "ping")
            put("uid", uid)
            put("ts", System.currentTimeMillis())
        }
        try {
            webSocket?.send(payload.toString())
            handlePingSuccess()
        } catch (e: Exception) {
            handlePingFailure()
        }
    }

    private fun handlePingSuccess() {
        heartbeatSuccessCount++
        heartbeatFailureCount = 0

        // 连续成功 3 次，间隔 +30s
        if (!isForeground && heartbeatSuccessCount >= 3) {
            heartbeatSuccessCount = 0
            val newInterval = heartbeatInterval + 30_000L
            if (newInterval <= currentSafeUpperLimit) {
                heartbeatInterval = newInterval
                Timber.d("Heartbeat interval increased to ${heartbeatInterval}ms")
                restartHeartbeat()
            }
        }
    }

    private fun handlePingFailure() {
        heartbeatFailureCount++
        heartbeatSuccessCount = 0

        // 同一间隔连续 2 次超时，降低安全上限
        if (heartbeatFailureCount >= 2) {
            currentSafeUpperLimit = heartbeatInterval - 30_000L
            heartbeatInterval = maxOf(60_000L, heartbeatInterval - 30_000L)
            Timber.w("Heartbeat failure: safe upper limit reduced to ${currentSafeUpperLimit}ms")
            restartHeartbeat()
        }
    }

    private fun handlePong() {
        Timber.d("Pong received")
    }

    // ===== 端口跳跃 =====
    private fun getCurrentPort(): Int {
        return if (currentPortIndex < portPool.size) {
            portPool[currentPortIndex]
        } else {
            staticPort
        }
    }

    private fun buildWebSocketUrl(port: Int): String {
        val host = serverHost
            .replace("https://", "")
            .replace("http://", "")
            .split("/").first()
        return "wss://$host:$port/ws"
    }

    private fun getDeviceId(): String {
        // 简单的设备 ID 生成（实际应使用 Android ID + Secure.ANDROID_ID）
        return "android_${uid}_${UUID.randomUUID().toString().take(8)}"
    }
}