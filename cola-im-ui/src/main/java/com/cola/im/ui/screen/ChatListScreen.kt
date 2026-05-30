package com.cola.im.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cola.im.ui.component.ColaAvatar
import com.cola.im.ui.model.ConversationItem
import com.cola.im.ui.theme.*
import com.cola.im.ui.util.TimeFormatter

/**
 * 会话列表（默认首页）
 * 垂直排列，每行包含头像 / 昵称+摘要 / 时间
 */
@Composable
fun ChatListScreen(
    conversations: List<ConversationItem>,
    onConversationClick: (ConversationItem) -> Unit,
    onFabClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("消息", fontWeight = FontWeight.SemiBold) },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                backgroundColor = ColaPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建聊天", tint = ColaOnPrimary)
            }
        },
        backgroundColor = MaterialTheme.colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colors.surface)
        ) {
            items(conversations, key = { it.conversationId }) { conv ->
                ConversationItemRow(
                    item = conv,
                    onClick = { onConversationClick(conv) }
                )
                Divider(
                    modifier = Modifier.padding(start = 76.dp),
                    color = Divider,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
private fun ConversationItemRow(
    item: ConversationItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像 48x48 圆角10
        ColaAvatar(
            url = item.avatar,
            nickname = item.nickname,
            size = 48.dp,
            corner = 10.dp
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 昵称
                Text(
                    text = item.nickname,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                // 时间
                Text(
                    text = TimeFormatter.formatRelative(item.lastMessageTime),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 摘要
                Text(
                    text = item.lastMessageSummary,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (item.unreadCount > 0) {
                    Spacer(Modifier.width(6.dp))
                    // 未读红点
                    Badge(
                        backgroundColor = ColaError,
                        contentColor = ColaOnPrimary
                    ) {
                        Text(
                            text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString(),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}