package com.cola.im.ui.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * 人性化时间格式化
 *
 * 规则：
 *  - < 5 秒    → "刚刚"
 *  - < 60 秒   → "X秒前"
 *  - < 60 分   → "X分钟前"
 *  - < 24 小时 → "X小时前"
 *  - < 7 天    → "X天前"
 *  - 今年      → "MM-dd HH:mm"
 *  - 往年      → "yyyy-MM-dd HH:mm"
 *
 * 聊天详情页的时间戳气泡显示完整时间: "yyyy-MM-dd HH:mm"
 */
object TimeFormatter {

    private const val JUST_NOW_THRESHOLD = 5_000L          // 5秒
    private const val SECOND = 1_000L
    private const val MINUTE = 60 * SECOND
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR
    private const val WEEK = 7 * DAY

    fun formatRelative(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        if (diff < 0) return "刚刚"

        return when {
            diff < JUST_NOW_THRESHOLD -> "刚刚"
            diff < MINUTE -> "${diff / SECOND}秒前"
            diff < HOUR -> "${diff / MINUTE}分钟前"
            diff < DAY -> "${diff / HOUR}小时前"
            diff < WEEK -> "${diff / DAY}天前"
            isSameYear(timestamp, now) -> SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
            else -> SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }

    /**
     * 聊天详情页时间戳：完整时间
     */
    fun formatFull(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    private fun isSameYear(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
    }
}