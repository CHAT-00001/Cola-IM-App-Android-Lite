package com.cola.im.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cola.im.ui.component.ColaAvatar
import com.cola.im.ui.model.Contact
import com.cola.im.ui.theme.*

/**
 * 联系人详情页
 * 点击聊天详情页的头像进入此页
 */
@Composable
fun ContactDetailScreen(
    contact: Contact,
    onBack: () -> Unit,
    onSendMessage: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("详细资料", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.5.dp
            )
        },
        backgroundColor = ColaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // 头像
            ColaAvatar(
                url = contact.avatar,
                nickname = contact.nickname,
                size = 80.dp,
                corner = 16.dp
            )
            Spacer(Modifier.height(16.dp))

            // 昵称
            Text(
                text = contact.nickname,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (!contact.bio.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = contact.bio,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(24.dp))

            // 操作按钮
            Button(
                onClick = onSendMessage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = ColaPrimary),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = ColaOnPrimary)
                Spacer(Modifier.width(8.dp))
                Text("发消息", color = ColaOnPrimary, fontSize = 16.sp)
            }

            Spacer(Modifier.height(32.dp))

            // 详细信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp
            ) {
                Column {
                    if (!contact.phone.isNullOrBlank()) {
                        DetailRow(label = "手机号", value = contact.phone)
                        Divider(color = Divider, thickness = 0.5.dp)
                    }
                    DetailRow(label = "ID", value = contact.id)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 15.sp, color = TextSecondary)
        Text(text = value, fontSize = 15.sp, color = TextPrimary)
    }
}