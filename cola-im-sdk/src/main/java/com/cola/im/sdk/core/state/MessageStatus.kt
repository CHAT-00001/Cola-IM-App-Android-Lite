package com.cola.im.sdk.core.state

/**
 * 消息状态枚举——"永不丢失"双层 ACK 状态机
 *
 * 状态流转：
 * SENDING → SUCCESS (收到 Server_ACK)
 * SENDING → FAILED (超时/断线)
 * SUCCESS → READ (收到 Read_ACK / 视觉停留报告)
 */
enum class MessageStatus {
    /** 发送中（乐观写入后） */
    SENDING,
    /** 发送成功（已收到 Server_ACK） */
    SUCCESS,
    /** 发送失败（超时/断线） */
    FAILED,
    /** 已读（接收端视觉停留超过 1000ms） */
    READ
}