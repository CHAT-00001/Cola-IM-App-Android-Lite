package com.cola.im.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cola.im.ui.component.ColaAvatar
import com.cola.im.ui.component.MessageAvatar
import com.cola.im.ui.model.*
import com.cola.im.ui.theme.*

/**
 * 聊天详情页
 * 气泡聊天布局，己方右对齐，系统消息居中浅色气泡字号12
 * 可扩展多种消息类型
 */
@Composable
fun ChatDetailScreen(
    conversationTitle: String,
    messages: List<UiMessage>,
    onSendText: (String) -> Unit,
    onAvatarClick: (String) -> Unit,        // senderId -> 联系人详情
    onSettingsClick: () -> Unit,            // 右上角 ... -> 聊天设置
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    // 新消息时自动滑到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = conversationTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.MoreVert, contentDescription = "聊天设置")
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.5.dp
            )
        },
        bottomBar = {
            // 底部输入栏
            Surface(
                elevation = 2.dp,
                color = MaterialTheme.colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入消息...") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                onSendText(inputText.trim())
                                inputText = ""
                            }
                        }),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = ColaPrimary,
                            unfocusedBorderColor = Divider
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendText(inputText.trim())
                                inputText = ""
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            tint = if (inputText.isNotBlank()) ColaPrimary else TextSecondary
                        )
                    }
                }
            }
        },
        backgroundColor = ColaBackground
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubbleItem(
                    message = msg,
                    onAvatarClick = { onAvatarClick(msg.senderId) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// 消息气泡
// ═══════════════════════════════════════════════════

@Composable
fun MessageBubbleItem(
    message: UiMessage,
    onAvatarClick: () -> Unit
) {
    when (message.side) {
        MessageSide.SELF -> SelfBubble(message)
        MessageSide.OTHER -> OtherBubble(message, onAvatarClick)
        MessageSide.SYSTEM -> SystemBubble(message)
    }
}

/**
 * 自己发送的消息 — 右对齐
 */
@Composable
private fun SelfBubble(message: UiMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        // 状态指示（发送中 / 失败）
        if (message.isFailed) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "发送失败",
                tint = ColaError,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 4.dp)
                    .size(16.dp)
            )
        } else if (message.isSending) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 4.dp)
                    .size(14.dp),
                strokeWidth = 1.5.dp,
                color = TextSecondary
            )
        }

        MessageContentBubble(
            content = message.content,
            isSelf = true,
            timestamp = message.timestamp
        )
    }
}

/**
 * 对方发送的消息 — 左对齐，带头像
 */
@Composable
private fun OtherBubble(message: UiMessage, onAvatarClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        MessageAvatar(
            url = message.senderAvatar,
            nickname = message.senderName,
            onClick = onAvatarClick
        )
        Spacer(Modifier.width(8.dp))
        MessageContentBubble(
            content = message.content,
            isSelf = false,
            timestamp = message.timestamp
        )
    }
}

/**
 * 系统消息 — 居中浅色气泡，字号12
 */
@Composable
private fun SystemBubble(message: UiMessage) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(BubbleSystem.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = when (val c = message.content) {
                    is MessageContent.Text -> c.text
                    else -> "系统消息"
                },
                fontSize = 12.sp,
                color = TextSystem,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════
// 内容气泡
// ═══════════════════════════════════════════════════

@Composable
private fun MessageContentBubble(
    content: MessageContent,
    isSelf: Boolean,
    timestamp: Long
) {
    val bgColor = if (isSelf) BubbleSelf else BubbleOther
    val textColor = TextPrimary
    val cornerShape = if (isSelf) {
        RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp)
    } else {
        RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp)
    }

    Column(
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(cornerShape)
                .background(bgColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            when (content) {
                is MessageContent.Text -> Text(
                    text = content.text,
                    fontSize = 18.sp,
                    color = textColor
                )
                is MessageContent.Photo -> ChatPhoto(content)
                is MessageContent.Video -> ChatVideo(content)
                is MessageContent.Emoji -> Text(
                    text = content.emoji,
                    fontSize = 36.sp
                )
                is MessageContent.Sticker -> ChatSticker(content)
                is MessageContent.BusinessCard -> ChatBusinessCard(content)
                is MessageContent.Location -> ChatLocation(content)
                is MessageContent.Voice -> ChatVoice(content)
                is MessageContent.Danmaku -> Text(
                    text = content.text,
                    fontSize = 18.sp,
                    color = textColor
                )
                is MessageContent.Gift -> ChatGift(content)
                is MessageContent.RedPacket -> ChatRedPacket(content)
                is MessageContent.Transfer -> ChatTransfer(content)
                is MessageContent.Unknown -> Text(
                    text = "[不支持的消息类型: ${content.rawType}]",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// 各消息类型渲染
// ═══════════════════════════════════════════════════

@Composable
private fun ChatPhoto(content: MessageContent.Photo) {
    val painter = rememberAsyncImagePainter(content.thumbUrl ?: content.url)
    Icon(
        painter = painter,
        contentDescription = "照片",
        modifier = Modifier.sizeIn(maxWidth = 200.dp, maxHeight = 300.dp),
        tint = Color.Unspecified
    )
}

@Composable
private fun ChatVideo(content: MessageContent.Video) {
    Box(contentAlignment = Alignment.Center) {
        val painter = rememberAsyncImagePainter(content.thumbUrl)
        Icon(
            painter = painter,
            contentDescription = "视频",
            modifier = Modifier.sizeIn(maxWidth = 200.dp, maxHeight = 300.dp),
            tint = Color.Unspecified
        )
        Icon(
            Icons.Default.PlayCircle,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ChatSticker(content: MessageContent.Sticker) {
    val painter = rememberAsyncImagePainter(content.url)
    Icon(
        painter = painter,
        contentDescription = content.name ?: "表情包",
        modifier = Modifier.sizeIn(maxWidth = 150.dp, maxHeight = 150.dp),
        tint = Color.Unspecified
    )
}

@Composable
private fun ChatBusinessCard(content: MessageContent.BusinessCard) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColaAvatar(url = content.avatar, nickname = content.nickname, size = 36.dp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(content.nickname, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("名片", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun ChatLocation(content: MessageContent.Location) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = ColaPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = content.address ?: "${content.lat}, ${content.lng}",
            fontSize = 16.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun ChatVoice(content: MessageContent.Voice) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(36.dp)
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = null,
            tint = ColaPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${content.duration}″",
            fontSize = 16.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun ChatGift(content: MessageContent.Gift) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (content.icon != null) {
            val painter = rememberAsyncImagePainter(content.icon)
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified
            )
        }
        Spacer(Modifier.width(4.dp))
        Column {
            Text(content.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("x${content.count}", fontSize = 12.sp, color = ColaError)
        }
    }
}

@Composable
private fun ChatRedPacket(content: MessageContent.RedPacket) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ColaError.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CardGiftcard,
                contentDescription = null,
                tint = ColaError,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(content.title, fontSize = 14.sp, color = ColaError, fontWeight = FontWeight.Medium)
                Text(
                    if (content.isReceived) "已领取" else "红包",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ChatTransfer(content: MessageContent.Transfer) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ColaPrimary.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = ColaPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text("转账 ¥${"%.2f".format(content.amount)}", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(
                    when (content.status) {
                        TransferStatus.PENDING -> "待收款"
                        TransferStatus.SUCCESS -> "已收款"
                        TransferStatus.REFUNDED -> "已退还"
                    },
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}