# Cola-IM UI 开发文档

## 模块概览

`cola-im-ui` 是基于 Jetpack Compose 构建的 IM（即时通讯）UI 组件库，提供聊天应用中常见的界面组件和页面。

## 模块结构

```
cola-im-ui/
├── build.gradle           # 模块构建配置
├── .gitignore
├── DEV.md                 # 本开发文档
└── src/main/java/com/cola/im/ui/
    ├── MainActivity.kt         # Demo 入口 Activity
    ├── model/
    │   └── UiModels.kt         # UI 数据模型定义
    ├── theme/
    │   └── Theme.kt            # 主题与颜色定义
    ├── util/
    │   └── TimeFormatter.kt    # 时间格式化工具
    ├── component/
    │   └── Avatar.kt           # 头像组件
    ├── screen/
    │   ├── ChatListScreen.kt       # 会话列表页
    │   ├── ChatDetailScreen.kt     # 聊天详情页（气泡）
    │   ├── ContactDetailScreen.kt  # 联系人详情页
    │   └── ChatSettingsScreen.kt   # 聊天设置页
    └── navigation/
        └── NavGraph.kt         # 占位导航图
```

## Screen 组件介绍

### 1. ChatListScreen – 会话列表

- 每个 Item 包含：头像、昵称、最后一条消息摘要、时间、未读数角标
- 支持 FAB（新建聊天）
- 时间格式化：当日显时分，昨天显"昨天"，更早显日期

### 2. ChatDetailScreen – 聊天气泡页

- **己方消息**：右对齐，蓝色气泡，右上圆角 4dp
- **对方消息**：左对齐，带头像，白色气泡，左上圆角 4dp
- **系统消息**：居中，浅色小字号背景
- **发送状态**：发送中显示转圈，失败显示警告图标
- **底部输入栏**：内嵌 `OutlinedTextField` + 发送按钮（回车键可发送）
- **消息类型**：文本、图片、视频、表情、位置、语音、礼物、红包、转账、名片、表情包、弹幕等

### 3. ContactDetailScreen – 联系人详情

- 大尺寸头像 + 昵称 + 签名
- "发消息"按钮
- 手机号、ID 卡片信息

### 4. ChatSettingsScreen – 聊天设置

- 群组头像 + 群名称
- 成员列表（点击可跳转联系人详情）
- 清空聊天记录操作项

## 导航方案

当前使用 `NavGraph.kt` 中定义的 `Screen` 密封类和 `ColaIMNavHost` 组合函数作为占位导航。集成时可根据项目实际需要替换为：

- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Voyager](https://github.com/adrielcafe/voyager)
- [Decompose](https://github.com/arkivanov/Decompose)

### 集成示例

```kotlin
// 在 Activity 中
setContent {
    ColaIMTheme {
        ColaIMNavHost(
            conversations = myConversations,
            onConversationClick = { /* 导航到聊天详情 */ },
            onAvatarClick = { /* 导航到联系人详情 */ },
            currentScreen = Screen.ChatList
        )
    }
}
```

## 主题定制

定义在 `Theme.kt`，主要颜色变量：

| Token | 默认值 | 用途 |
|---|---|---|
| `ColaPrimary` | `#FF007AFF` | 主色调（己方气泡、按钮） |
| `ColaBackground` | `#FFF5F5F5` | 页面背景 |
| `BubbleSelf` | `#FF007AFF` | 己方气泡背景 |
| `BubbleOther` | `#FFFFFFFF` | 对方气泡背景 |
| `ColaError` | `#FFFF3B30` | 错误/红包/警告 |

## 依赖

- Jetpack Compose （BOM 管理版本）
- Coil （图片加载）
- Material Icons Extended

## 开发注意事项

1. 所有 Screen 接受回调而非 ViewModel 引用，保持可测试性
2. 数据模型位于 `model/`，与 SDK 层 Entity 解耦
3. 时间格式化使用 `TimeFormatter.kt`，按国内习惯处理
4. 此模块作为独立 UI 组件库，不包含网络/数据库/业务逻辑