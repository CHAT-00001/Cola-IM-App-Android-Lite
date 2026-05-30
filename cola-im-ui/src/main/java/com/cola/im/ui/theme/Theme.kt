package com.cola.im.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── 调色板 ──────────────────────────────────────────
val ColaPrimary = Color(0xFF07C160)        // 微信绿 — 主色
val ColaPrimaryVariant = Color(0xFF06AD56)
val ColaOnPrimary = Color.White

val ColaSecondary = Color(0xFF576B95)
val ColaSecondaryVariant = Color(0xFF3D537A)
val ColaOnSecondary = Color.White

val ColaBackground = Color(0xFFEDEDED)     // 聊天背景
val ColaSurface = Color.White
val ColaOnBackground = Color(0xFF1A1A1A)
val ColaOnSurface = Color(0xFF1A1A1A)

val ColaError = Color(0xFFE53935)

// 气泡色
val BubbleSelf = Color(0xFF95EC69)          // 自己消息气泡
val BubbleOther = Color.White               // 对方消息气泡
val BubbleSystem = Color(0xFFD6D6D6)        // 系统消息气泡

// 文字色
val TextPrimary = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF8E8E93)
val TextSystem = Color(0xFF6D6D72)

// 分割线
val Divider = Color(0xFFE5E5E5)

val DarkColaBackground = Color(0xFF111111)
val DarkColaSurface = Color(0xFF1E1E1E)
val DarkBubbleSelf = Color(0xFF056E3F)
val DarkBubbleOther = Color(0xFF2C2C2E)

private val LightColorScheme = lightColors(
    primary = ColaPrimary,
    primaryVariant = ColaPrimaryVariant,
    onPrimary = ColaOnPrimary,
    secondary = ColaSecondary,
    secondaryVariant = ColaSecondaryVariant,
    onSecondary = ColaOnSecondary,
    background = ColaBackground,
    surface = ColaSurface,
    onBackground = ColaOnBackground,
    onSurface = ColaOnSurface,
    error = ColaError
)

private val DarkColorScheme = darkColors(
    primary = ColaPrimary,
    primaryVariant = ColaPrimaryVariant,
    onPrimary = ColaOnPrimary,
    secondary = ColaSecondary,
    secondaryVariant = ColaSecondaryVariant,
    onSecondary = ColaOnSecondary,
    background = DarkColaBackground,
    surface = DarkColaSurface,
    onBackground = ColaOnBackground,
    onSurface = ColaOnSurface,
    error = ColaError
)

@Composable
fun ColaIMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colors = colors,
        content = content
    )
}