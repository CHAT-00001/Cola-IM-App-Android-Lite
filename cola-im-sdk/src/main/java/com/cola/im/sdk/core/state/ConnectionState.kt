package com.cola.im.sdk.core.state

/**
 * 连接状态枚举
 */
enum class ConnectionState {
    /** 未连接 */
    DISCONNECTED,
    /** 连接中（WebSocket 握手） */
    CONNECTING,
    /** 已连接但未认证 */
    CONNECTED,
    /** 已认证（鉴权通过，可收发消息） */
    AUTHENTICATED,
    /** 重连中 */
    RECONNECTING,
    /** 连接失败 */
    FAILED
}