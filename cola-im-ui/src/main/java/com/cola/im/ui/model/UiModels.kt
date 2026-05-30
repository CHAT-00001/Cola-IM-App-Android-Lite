package com.cola.im.ui.model

/**
 * UI 层消息类型 — 可扩展
 * 参考微信/iMessage 支持的类型
 */
sealed class MessageContent {
    data class Text(val text: String) : MessageContent()
    data class Photo(val url: String, val thumbUrl: String? = null, val width: Int = 0, val height: Int = 0) : MessageContent()
    data class Video(val url: String, val thumbUrl: String? = null, val duration: Int = 0) : MessageContent()
    data class Emoji(val emoji: String) : MessageContent()
    data class Sticker(val url: String, val name: String? = null) : MessageContent()
    data class BusinessCard(val userId: String, val nickname: String, val avatar: String? = null) : MessageContent()
    data class Location(val lat: Double, val lng: Double, val address: String? = null) : MessageContent()
    data class Voice(val url: String, val duration: Int = 0) : MessageContent()
    data class Danmaku(val text: String, val color: Long = 0xFFFFFFFF) : MessageContent()
    data class Gift(val giftId: String, val name: String, val icon: String? = null, val count: Int = 1) : MessageContent()
    data class RedPacket(val packetId: String, val title: String = "恭喜发财，大吉大利", val isReceived: Boolean = false) : MessageContent()
    data class Transfer(val transferId: String, val amount: Double, val note: String? = null, val status: TransferStatus = TransferStatus.PENDING) : MessageContent()

    /** 未知类型占位 — 可扩展 */
    data class Unknown(val rawType: String, val rawJson: String? = null) : MessageContent()
}

enum class TransferStatus { PENDING, SUCCESS, REFUNDED }

/**
 * 消息发送方向
 */
enum class MessageSide {
    /** 自己发送（右侧气泡） */
    SELF,
    /** 对方发送（左侧气泡） */
    OTHER,
    /** 系统消息（居中） */
    SYSTEM
}

/**
 * UI 层消息模型
 */
data class UiMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String? = null,
    val content: MessageContent,
    val side: MessageSide,
    val timestamp: Long,            // unix millis
    val isRead: Boolean = false,
    val isSending: Boolean = false,
    val isFailed: Boolean = false
)

/**
 * 会话列表项
 */
data class ConversationItem(
    val conversationId: String,
    val avatar: String? = null,
    val nickname: String,
    val lastMessageSummary: String,
    val lastMessageTime: Long,       // unix millis
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false
)

/**
 * 联系人
 */
data class Contact(
    val id: String,
    val nickname: String,
    val avatar: String? = null,
    val bio: String? = null,
    val phone: String? = null
)

/**
 * 聊天设置
 */
data class ChatSettings(
    val isMuted: Boolean = false,
    val showNotifications: Boolean = true,
    val ringtone: String? = null,
    val isPinned: Boolean = false,
    val isSecretChat: Boolean = false
)