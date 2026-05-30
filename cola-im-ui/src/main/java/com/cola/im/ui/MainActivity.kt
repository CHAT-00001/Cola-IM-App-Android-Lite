package com.cola.im.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cola.im.ui.model.*
import com.cola.im.ui.navigation.ColaIMNavHost
import com.cola.im.ui.navigation.Screen
import com.cola.im.ui.theme.ColaIMTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ColaIMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ColaIMDemo()
                }
            }
        }
    }
}

/**
 * Demo 示例 —— 展示基础页面流转
 */
@Composable
private fun ColaIMDemo() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.ChatList) }

    // Dummy 数据
    val demoConversations = remember {
        listOf(
            ConversationItem(
                conversationId = "c1",
                avatar = null,
                nickname = "张三",
                lastMessageSummary = "好的，明天见！",
                lastMessageTime = System.currentTimeMillis() - 30_000,
                unreadCount = 2
            ),
            ConversationItem(
                conversationId = "c2",
                avatar = null,
                nickname = "技术群聊",
                lastMessageSummary = "李四: 最新的 PR 已经提交了",
                lastMessageTime = System.currentTimeMillis() - 3_600_000,
                unreadCount = 5
            ),
            ConversationItem(
                conversationId = "c3",
                avatar = null,
                nickname = "王五",
                lastMessageSummary = "收到，谢谢！",
                lastMessageTime = System.currentTimeMillis() - 86_400_000,
                unreadCount = 0
            )
        )
    }

    val demoMessages = remember {
        listOf(
            UiMessage(
                id = "m1",
                senderId = "user1",
                senderName = "我",
                senderAvatar = null,
                content = MessageContent.Text("你好，在吗？"),
                timestamp = System.currentTimeMillis() - 120_000,
                side = MessageSide.SELF
            ),
            UiMessage(
                id = "m2",
                senderId = "other",
                senderName = "张三",
                senderAvatar = null,
                content = MessageContent.Text("在的，有什么事吗？"),
                timestamp = System.currentTimeMillis() - 60_000,
                side = MessageSide.OTHER
            ),
            UiMessage(
                id = "m3",
                senderId = "user1",
                senderName = "我",
                senderAvatar = null,
                content = MessageContent.Text("明天下午三点见面聊？"),
                timestamp = System.currentTimeMillis() - 35_000,
                side = MessageSide.SELF,
                isSending = true
            ),
            UiMessage(
                id = "m4",
                senderId = "other",
                senderName = "张三",
                senderAvatar = null,
                content = MessageContent.Text("好的，明天见！"),
                timestamp = System.currentTimeMillis() - 30_000,
                side = MessageSide.OTHER
            ),
            UiMessage(
                id = "m5",
                senderId = "system",
                senderName = "系统",
                senderAvatar = null,
                content = MessageContent.Text("你已添加了张三，现在可以开始聊天了"),
                timestamp = System.currentTimeMillis() - 180_000,
                side = MessageSide.SYSTEM
            )
        )
    }

    ColaIMNavHost(
        conversations = demoConversations,
        messages = demoMessages,
        contact = Contact(
            id = "other",
            nickname = "张三",
            avatar = null,
            bio = "你好，我是张三",
            phone = "138****8888"
        ),
        members = listOf(
            Contact(id = "u1", nickname = "张三"),
            Contact(id = "u2", nickname = "李四"),
            Contact(id = "u3", nickname = "王五")
        ),
        onConversationClick = {
            currentScreen = Screen.ChatDetail(it.conversationId)
        },
        onFabClick = {
            // 新建聊天
        },
        onSendText = { text ->
            // 发送消息
        },
        onAvatarClick = { senderId ->
            currentScreen = Screen.ContactDetail(senderId)
        },
        onSettingsClick = {
            currentScreen = Screen.ChatSettings("c1")
        },
        onBack = {
            currentScreen = Screen.ChatList
        },
        onMemberClick = { contact ->
            currentScreen = Screen.ContactDetail(contact.id)
        },
        onClearChat = {
            // 清空聊天
        },
        onSendMessageToContact = {
            currentScreen = Screen.ChatList
        },
        currentScreen = currentScreen
    )
}