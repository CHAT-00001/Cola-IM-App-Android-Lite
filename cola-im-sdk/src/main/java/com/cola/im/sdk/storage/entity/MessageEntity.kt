package com.cola.im.sdk.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cola.im.sdk.core.state.MessageStatus

/**
 * 消息实体——单用户单库 Room Entity
 *
 * 索引：send_id + from_uid 用于幂等去重
 */
@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["send_id", "from_uid"], unique = true),
        Index(value = ["sync_id"], unique = true),
        Index(value = ["seq_no"]),
        Index(value = ["timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    @ColumnInfo(name = "send_id")
    val sendId: String,

    @ColumnInfo(name = "sync_id")
    val syncId: String = "",

    @ColumnInfo(name = "seq_no")
    val seqNo: Long = 0L,

    @ColumnInfo(name = "sync_at")
    val syncAt: Long = 0L,

    @ColumnInfo(name = "from_uid")
    val fromUid: String = "",

    @ColumnInfo(name = "to_target_id")
    val toTargetId: String = "",

    @ColumnInfo(name = "content")
    val content: String = "",

    @ColumnInfo(name = "content_type")
    val contentType: String = "text",

    @ColumnInfo(name = "status")
    val status: MessageStatus = MessageStatus.SENDING,

    @ColumnInfo(name = "is_encrypted")
    val isEncrypted: Boolean = false,

    @ColumnInfo(name = "crypto_noise")
    val cryptoNoise: String = "",

    @ColumnInfo(name = "channel")
    val channel: String = "",

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)