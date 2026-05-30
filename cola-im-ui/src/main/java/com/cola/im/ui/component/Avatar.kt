package com.cola.im.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

/**
 * 通用圆形头像组件
 *
 * @param url       图片 URL，null 或空则显示昵称首字
 * @param nickname  昵称（用于 fallback 首字 + 背景色）
 * @param size      头像尺寸，默认48dp
 * @param corner    圆角半径，默认10dp
 * @param onClick   点击回调（进入联系人详情页）
 */
@Composable
fun ColaAvatar(
    url: String?,
    nickname: String,
    size: Dp = 48.dp,
    corner: Dp = 10.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(corner)

    val clickModifier = if (onClick != null) modifier.clickable { onClick() } else modifier

    Box(
        modifier = clickModifier
            .size(size)
            .clip(shape)
            .background(avatarBgColor(nickname), shape),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = nickname,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Text(
                text = nickname.firstOrNull()?.toString() ?: "?",
                color = Color.White,
                fontSize = (size.value * 0.42).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 根据昵称生成稳定的背景色
 */
private val avatarColors = listOf(
    0xFF07C160, 0xFF576B95, 0xFFE53935, 0xFFFF9800, 0xFF9C27B0,
    0xFF00BCD4, 0xFF795548, 0xFF607D8B, 0xFFE91E63, 0xFF3F51B5
)

private fun avatarBgColor(name: String): Color {
    val idx = kotlin.math.abs(name.hashCode()) % avatarColors.size
    return Color(avatarColors[idx])
}

/**
 * 消息气泡两侧的头像（40dp）
 */
@Composable
fun MessageAvatar(
    url: String?,
    nickname: String,
    onClick: (() -> Unit)? = null
) = ColaAvatar(
    url = url,
    nickname = nickname,
    size = 40.dp,
    corner = 10.dp,
    onClick = onClick
)