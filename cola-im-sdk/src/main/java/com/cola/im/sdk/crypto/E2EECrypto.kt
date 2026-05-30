package com.cola.im.sdk.crypto

import timber.log.Timber

/**
 * 端到端加密（E2EE）—— Signal 协议骨架
 *
 * 消息三层封装：
 * 1. 信封层(Envelope)：send_id, sync_id, from_uid, to_target_id, channel（不加密）
 * 2. 消息体(Message)：实际内容（X3DH + Double Ratchet 加密）
 * 3. 负载(Payload)：密文 + crypto_noise（棘轮公钥参数）
 *
 * 当前为骨架实现，完整 Signal 协议集成需要引入 libsignal-client 库。
 */
class E2EECrypto {

    data class Envelope(
        val sendId: String,
        val syncId: String = "",
        val fromUid: String,
        val toTargetId: String,
        val channel: String = ""
    )

    data class EncryptedPayload(
        val ciphertext: ByteArray,
        val cryptoNoise: String   // Double Ratchet 公钥参数（JSON）
    )

    /**
     * 加密消息体
     *
     * @param plaintext 明文消息
     * @param fromUid 发送方 UID
     * @param toTargetId 接收方 UID/群 ID
     * @return 加密后的负载
     */
    fun encrypt(plaintext: ByteArray, fromUid: String, toTargetId: String): EncryptedPayload {
        // TODO: 集成 libsignal-client
        // 1. X3DH 密钥协商 → 获取会话密钥
        // 2. Double Ratchet 加密 → 输出密文
        // 3. 序列化棘轮公钥参数 → crypto_noise

        Timber.d("E2EE encrypt: from=$fromUid to=$toTargetId (skeleton)")
        return EncryptedPayload(
            ciphertext = plaintext,  // 骨架：透传明文
            cryptoNoise = "{}"
        )
    }

    /**
     * 解密消息体
     */
    fun decrypt(ciphertext: ByteArray, cryptoNoise: String, fromUid: String): ByteArray {
        // TODO: 集成 libsignal-client
        Timber.d("E2EE decrypt: from=$fromUid (skeleton)")
        return ciphertext  // 骨架：透传密文
    }

    /**
     * 构建信封层
     */
    fun buildEnvelope(
        sendId: String,
        fromUid: String,
        toTargetId: String,
        channel: String = ""
    ): Envelope {
        return Envelope(
            sendId = sendId,
            fromUid = fromUid,
            toTargetId = toTargetId,
            channel = channel
        )
    }
}