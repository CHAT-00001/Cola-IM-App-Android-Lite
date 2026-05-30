package com.cola.im.ui.navigation

import androidx.compose.runtime.Composable
import com.cola.im.ui.model.*
import com.cola.im.ui.screen.*

/**
 * 占位导航图
 *
 * 由于 Compose 导航方案各异（Navigation Compose / Voyager / Decompose），
 * 此处仅定义一个接口 + 一个组合函数接收各 Screen 回调，
 * 实际集成时替换为所选导航库的路由调用。
 */

sealed class Screen {
    data object ChatList : Screen()
    data class ChatDetail(val conversationId: String) : Screen()
    data class ContactDetail(val contactId: String) : Screen()
    data class ChatSettings(val conversationId: String) : Screen()
}

/**
 * 应用入口 Composable
 *
 * 接收各页面需要的回调，由外部集成方（Activity/Fragment）注入，
 * 集成方可将其映射为导航操作。
 */
@Composable
fun ColaIMNavHost(
    // Dummy 数据 —— 演示用，实际应从 ViewModel / Repository 获取
    conversations: List<ConversationItem> = emptyList(),
    messages: List<UiMessage> = emptyList(),
    contact: Contact = Contact(id = "", nickname = "未知用户"),
    members: List<Contact> = emptyList(),

    // 页面间回调
    onConversationClick: (ConversationItem) -> Unit = {},
    onFabClick: () -> Unit = {},
    onSendText: (String) -> Unit = {},
    onAvatarClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onMemberClick: (Contact) -> Unit = {},
    onClearChat: () -> Unit = {},
    onSendMessageToContact: () -> Unit = {},

    // 当前显示哪个页面（简易状态路由，真实项目应替换为 Navigation Compose）
    currentScreen: Screen = Screen.ChatList
) {
    when (currentScreen) {
        is Screen.ChatList -> ChatListScreen(
            conversations = conversations,
            onConversationClick = onConversationClick,
            onFabClick = onFabClick
        )
        is Screen.ChatDetail -> ChatDetailScreen(
            conversationTitle = "聊天",
            messages = messages,
            onSendText = onSendText,
            onAvatarClick = onAvatarClick,
            onSettingsClick = onSettingsClick,
            onBack = onBack
        )
        is Screen.ContactDetail -> ContactDetailScreen(
            contact = contact,
            onBack = onBack,
            onSendMessage = onSendMessageToContact
        )
        is Screen.ChatSettings -> ChatSettingsScreen(
            groupAvatar = null,
            groupName = "聊天详情",
            members = members,
            onMemberClick = onMemberClick,
            onBack = onBack,
            onClearChat = onClearChat
        )
    }
}