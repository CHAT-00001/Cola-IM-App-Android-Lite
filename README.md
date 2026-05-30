# 可乐 IM (Kele IM / Cola IM)

> **一款基于直播 + 短视频 + 即时通讯的 Android 社交平台**
>
> 包名：`com.yunbao.phonelive` | 版本：`0.1.0` | 编译 SDK：API 31 | 目标 SDK：API 34

---

## 目录

- [项目简介](#项目简介)
- [应用截图](#应用截图)
- [项目结构](#项目结构)
- [技术栈](#技术栈)
- [集成服务](#集成服务)
- [快速开始](#快速开始)
- [构建配置](#构建配置)
- [模块说明](#模块说明)
- [Cola-IM SDK](#cola-im-sdk)
- [发布流程](#发布流程)
- [贡献指南](#贡献指南)
- [许可](#许可)

---

## 项目简介

可乐 IM 是一个功能完备的 Android 社交娱乐平台，核心功能涵盖：

- **直播互动**：主播推流、观众拉流、连麦、礼物打赏、弹幕聊天
- **短视频**：视频录制、编辑、发布、播放、点赞评论
- **即时通讯 (IM)**：私信聊天、群聊、系统通知，基于腾讯云 IM + 自定义 Cola-IM SDK
- **美颜特效**：基于美狐 MHSDK 的人脸美颜、滤镜特效
- **商城系统**：商品展示、下单购买、支付、物流跟踪（买家/卖家双端）
- **语音识别**：集成百度语音，支持语音输入转文字
- **小游戏**：内置互动游戏模块
- **用户体系**：登录注册、个人主页、关注/粉丝、排行榜、青少年模式

### 项目定位

```text
原项目为 YunBao（云豹）直播系统，当前改造为"可乐 IM"品牌，
以即时通讯为核心，融合直播、短视频、商城等泛娱乐社交功能。
```

---

## 项目结构

```
├── app/                          # 应用主入口（Application）
│   ├── build.gradle              # 主模块构建配置
│   └── src/main/
│       ├── AndroidManifest.xml   # 全局 Manifest（权限、Activity、Service）
│       ├── java/com/yunbao/phonelive/
│       │   ├── AppContext.java        # Application 入口
│       │   └── activity/              # Launcher、WX回调等
│       └── res/
│           ├── values/strings.xml     # 中文字符串
│           ├── values-en/             # 英文
│           ├── values-es/             # 西班牙语
│           ├── values-fr/             # 法语
│           ├── values-ja/             # 日语
│           ├── values-ko/             # 韩语
│           ├── values-th/             # 泰语
│           └── values-tw/             # 繁体中文
│
├── main/                         # 主功能模块（登录、个人主页、设置、活动等）
├── video/                        # 短视频模块
├── live/                         # 直播模块
├── common/                       # 通用基础库（网络、图片、UI组件、工具类）
├── im/                           # 即时通讯模块（基于腾讯云 IM SDK）
├── cola-im-sdk/                  # 🆕 Cola-IM 自研 SDK（端到端加密、本地存储、MVVM 架构）
├── baidu/                        # 百度语音识别模块
├── beauty/                       # 美颜模块（美狐 MHSDK）
├── mall/                         # 商城模块
├── game/                         # 游戏模块
├── libs/                         # 第三方本地依赖（aar / jar）
├── gradle/wrapper/               # Gradle Wrapper
│
├── build.gradle                  # 根构建脚本
├── config.gradle                 # 全局配置（版本号、SDK密钥）
├── dependencies.gradle           # 依赖版本管理
├── Mob.gradle                    # MobSDK 配置
├── settings.gradle               # 模块注册
├── gradle.properties             # Gradle 属性
├── yunbao.jks                    # 签名文件
└── local.properties              # 本地 SDK 路径
```

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| **语言** | Java + Kotlin | 1.6.10 |
| **构建工具** | Gradle + Android Gradle Plugin | 8.5.0 / 8.5.0 |
| **最低 SDK** | Android 6.0 (API 23) | - |
| **目标 SDK** | Android 14 (API 34) | - |
| **网络请求** | OkHttp | 3.11.0 |
| **JSON 解析** | FastJson | 1.1.70.android |
| **事件总线** | EventBus | 3.0.0 |
| **图片加载** | Glide + Glide Transformations | 4.9.0 |
| **图片裁剪** | uCrop | - |
| **图片压缩** | Luban | - |
| **页面指示器** | MagicIndicator | - |
| **轮播图** | Banner | 1.4.9 |
| **动画** | SVGAPlayer + Gif Drawable | 2.6.1 |
| **地图** | 腾讯地图 Vector SDK | 4.2.8 |
| **定位** | 腾讯定位 SDK | 6.2.5.3 |
| **下拉刷新** | SmartRefreshLayout | 2.0.5 |
| **ViewPager** | Flexbox | 3.0.0 |
| **图片加载** | Coil | 2.1.0 |
| **亚马逊存储** | AWS S3 + Cognito | 2.52.1 |
| **支付** | Braintree PayPal Drop-in | 6.0.0 |
| **网络请求** | Fuel (Kotlin) | 2.3.1 |
| **Toast** | ToastUtils | 10.3 |
| **HTTP 客户端** | Apache HTTP Legacy | - |
| **数据库** | Room (Cola-IM SDK) | - |
| **加密** | 自定义 E2EE (Cola-IM SDK) | - |

---

## 集成服务

| 服务商 | 用途 | 配置键 |
|--------|------|--------|
| **腾讯云直播 LiteAVSDK** | 直播推流/播放、短视频录制/播放 | `LiteAVSDK_Professional` aar |
| **腾讯云 IM** | 即时通讯（私信、群聊） | `TxIMAppId` |
| **腾讯 TPNS 推送** | 移动推送通知 | `XG_ACCESS_ID` / `XG_ACCESS_KEY` |
| **腾讯 Bugly** | 崩溃收集与统计 | `buglyAppId` |
| **腾讯地图** | 定位、地图展示、逆地址解析 | `txMapAppKey` |
| **微信开放平台** | 微信登录、分享、支付 | `WxAppId` / `WxAppSecret` |
| **QQ 互联** | QQ 登录、分享 | `QQAppId` / `QQAppKey` |
| **MobSDK / ShareSDK** | 社交分享 | `MobAppKey` / `MobAppSecret` |
| **友盟统计** | 用户行为统计分析 | `umengAppKey` |
| **百度语音** | 语音识别（语音转文字） | `baiduAppId` / `baiduAppKey` |
| **OpenInstall** | 带参数安装、上下级邀请 | `openinstallAppKey` |
| **美狐 MHSDK** | 美颜、滤镜、特效 | - |
| **七牛云存储** | 文件/图片/视频上传 | `qiniu-sdk` |
| **亚马逊 AWS** | S3 文件存储 | - |
| **Braintree / PayPal** | 海外支付 | - |

---

## 快速开始

### 环境要求

| 工具 | 要求 |
|------|------|
| Android Studio | Hedgehog (2023.1.1) 或更高 |
| JDK | 11+（推荐 17 或 21） |
| Gradle | 8.5（使用 Wrapper 自动下载） |
| Android SDK | API 31 (compileSdk) + API 34 (targetSdk) |

### 构建步骤

```bash
# 1. 克隆项目
git clone <repository-url>
cd 8.0.8

# 2. 配置第三方密钥
#    编辑 config.gradle 中的 manifestPlaceholders，替换为您的密钥

# 3. 构建 Debug APK
./gradlew assembleDebug

# 4. 构建 Release APK
./gradlew assembleRelease
```

生成的 APK 位于 `app/build/outputs/apk/` 目录下。

---

## 构建配置

### 签名

签名文件位于项目根目录 `yunbao.jks`：

```groovy
keyAlias     = 'phonelive'
keyPassword  = 'phonelive'
storePassword = 'phonelive'
```

> **注意**：debug 构建默认使用 release 签名配置。

### APK 命名

打包时自动重命名格式：
```
可乐-IM-demo_v{versionName}_{yyMMddHHmm}.apk
```

例如：`可乐-IM-demo_v0.1.0_2605300708.apk`

---

## 模块说明

### `main` — 主功能模块
- 登录 / 注册 / 找回密码
- 个人主页（编辑资料、头像、签名、印象标签）
- 设置（修改密码、注销账号、青少年模式）
- 搜索（用户、内容）
- 活动发布 / 浏览 / 视频录制
- 排行榜、关注/粉丝、直播推荐
- 每日任务、家族/Family 管理

### `video` — 短视频模块
- 视频录制（Camera + 美颜）
- 视频播放（滑动列表）
- 视频编辑与发布

### `live` — 直播模块
- 主播推流（摄像头、屏幕录制）
- 观众拉流播放
- 直播间互动（弹幕、礼物、守护、管理员、禁言）
- 连麦、PK
- 直播记录回放

### `im` — 即时通讯模块
- 私信聊天（文本、图片、语音、位置）
- 群聊
- 系统通知（点赞、评论、@提到、消息通知）
- 基于腾讯云 IM SDK

### `common` — 通用基础库
- 网络请求封装（OkHttp）
- 图片加载（Glide + Coil）
- 工具类
- 公共 UI 组件（刷新、加载、轮播、指示器、弹窗）
- 第三方服务初始化（Bugly、友盟、微信、QQ、Mob、OpenInstall）

### `beauty` — 美颜模块
- 基于美狐 MHSDK 的美颜滤镜
- 大眼、瘦脸、磨皮、美白等特效

### `mall` — 商城模块
- 买家端：商品浏览、下单、支付、退款、评价
- 卖家端：商品管理、订单处理、提现

### `baidu` — 语音识别模块
- 基于百度语音 SDK 的语音输入转文字

### `game` — 游戏模块
- 内置互动小游戏

---

## Cola-IM SDK

`cola-im-sdk/` 是自研的即时通讯 SDK，采用 **侧载模式（Sideload Mode）** 嵌入宿主应用，不依赖任何业务框架。

### 架构设计

```
┌─────────────────────────────────────────────────┐
│                  ColaIM (Facade)                  │
├─────────────────────────────────────────────────┤
│  ConnectionManager    │    MessageRepository     │
│  (WebSocket 长连接)    │    (Room 本地存储)        │
├───────────────────────┴─────────────────────────┤
│  CoreEngine (MVI)     │    E2EECrypto (加密)     │
├─────────────────────────────────────────────────┤
│  Room Database (SQLite)    │    Proto (协议)      │
└─────────────────────────────────────────────────┘
```

### 核心组件

| 组件 | 说明 |
|------|------|
| `ColaIM` | SDK 门面（Facade），提供 `init()` / `sendMessage()` / `observeMessages()` API |
| `ConnectionManager` | WebSocket 连接管理，自动重连，心跳保活 |
| `MessageRepository` | 消息仓库（Single Source of Truth），写 Room → 发网络 → Flow 通知 UI |
| `CoreEngine` | MVI 架构核心引擎，管理消息状态机 |
| `E2EECrypto` | 端到端加密骨架（X3DH + Double Ratchet，待集成 libsignal-client） |
| `AppDatabase` | Room 数据库，存储消息实体 |
| `cola_im.proto` | Protobuf V3 协议定义（Envelope + MessageBody + Ack） |

### 协议栈

```
应用层 (Cola-IM SDK)
  ↓
Protobuf 序列化 (cola_im.proto)
  ↓
WebSocket (自定义长连接)
  ↓
TCP/TLS
```

消息结构为三层封装：
1. **Envelope（信封层）** — 路由信息，不加密
2. **MessageBody（消息体）** — 实际内容（可加密）
3. **Ack（确认消息）** — server_ack / client_ack / read_ack

---

## 发布流程

```bash
# 1. 更新版本号
#    编辑 config.gradle -> versionName

# 2. 构建 Release
./gradlew assembleRelease

# 3. APK 输出路径
#    app/build/outputs/apk/release/可乐-IM-demo_v{version}_{time}.apk
```

---

## 贡献指南

1. Fork 本项目
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交变更：`git commit -m 'feat: add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 创建 Pull Request

### 提交规范

本项目使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` Bug 修复
- `refactor:` 重构
- `docs:` 文档
- `style:` 代码样式
- `chore:` 构建/工具

---

## 许可

```
版权所有 © 2024-2026 可乐 IM Team

本项目为内部项目，未经授权不得用于商业用途。
```

---

> **相关链接**
>
> - 后端 API：`https://api2.damawei.com`
> - 腾讯云 IM 文档：https://cloud.tencent.com/product/im
> - 腾讯云直播 SDK：https://cloud.tencent.com/product/mlvb
> - 美狐 MHSDK：https://www.meihu.tech