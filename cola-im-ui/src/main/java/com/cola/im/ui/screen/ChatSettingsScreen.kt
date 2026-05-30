package com.cola.im.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * 聊天设置页（右上角 ... 进入）
 * 可跳转联系人详情、清空聊天、退出群聊等
 */
@Composable
fun ChatSettingsScreen(
    groupAvatar: String?,
    groupName: String,
    members: List<Contact>,
    onMemberClick: (Contact) -> Unit,
    onBack: () -> Unit,
    onClearChat: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("聊天详情", fontWeight = FontWeight.SemiBold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 群组信息
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ColaAvatar(
                            url = groupAvatar,
                            nickname = groupName,
                            size = 72.dp,
                            corner = 14.dp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = groupName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // 群成员（横滑待优化，先做垂直列表）
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "群成员 (${members.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(12.dp))
                        members.forEach { member ->
                            MemberRow(
                                contact = member,
                                onClick = { onMemberClick(member) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            // 操作项
            item { Spacer(Modifier.height(8.dp)) }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp
                ) {
                    Column {
                        SettingsActionRow(
                            icon = Icons.Default.Delete,
                            text = "清空聊天记录",
                            onClick = onClearChat,
                            tintColor = ColaError
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun MemberRow(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColaAvatar(
            url = contact.avatar,
            nickname = contact.nickname,
            size = 40.dp,
            corner = 8.dp
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = contact.nickname,
            fontSize = 15.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    tintColor: androidx.compose.ui.graphics.Color = ColaError
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = tintColor
        )
    }
}