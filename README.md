# 云豹直播 (YunBao Live) - Android 直播短视频社交平台

## 项目简介

云豹直播是一款功能完整的 Android 直播+短视频社交平台，集成直播推流/播放、短视频、即时通讯（IM）、美颜特效、商城、游戏等多元化功能模块，是一套完整的主播/用户双向互动的泛娱乐直播解决方案。

- **包名**: `com.yunbao.phonelive`
- **版本**: 8.0.8（versionCode: 203）
- **最低支持**: Android 6.0（API 23）
- **目标 SDK**: Android 14（API 34）
- **编译 SDK**: API 31

---

## 项目结构

```
├── app/                    # 应用主入口，依赖所有业务模块
├── main/                   # 主包（登录注册、个人主页、设置、搜索、活动等核心功能）
├── video/                  # 短视频模块（录制、播放、编辑）
├── live/                   # 直播模块（推流、连麦、礼物互动）
├── common/                 # 通用基础库（网络请求、图片加载、工具类、UI组件）
├── im/                     # 即时通讯模块（基于腾讯云 IM）
├── baidu/                  # 百度语音识别模块
├── beauty/                 # 美颜模块（基于美狐 MHSDK）
├── mall/                   # 商城模块（商品展示、购买）
├── game/                   # 游戏模块
├── libs/                   # 第三方本地依赖（aar/jar）
└── gradle/                 # Gradle Wrapper
```

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java + Kotlin 1.6.10 |
| 构建系统 | Gradle 8.5.0 + Android Gradle Plugin 8.5.0 |
| 网络请求 | OkHttp 3.11.0 |
| JSON 解析 | FastJson 1.1.70.android |
| 事件总线 | EventBus 3.0.0 |
| 图片加载 | Glide 4.9.0 + Glide Transformations |
| 页面指示器 | MagicIndicator |
| 轮播图 | Banner 1.4.9 |
| 图片裁剪 | uCrop |
| 图片压缩 | Luban |
| 动画 | SVGAPlayer 2.6.1 / Gif Drawable |

---

## 集成服务

| 服务商 | 用途 |
|--------|------|
| **腾讯云直播** (LiteAVSDK) | 直播推流/播放、短视频录制/播放 |
| **腾讯云 IM** | 即时通讯（私信、群聊） |
| **腾讯 TPNS** | 移动推送通知 |
| **腾讯 Bugly** | 崩溃收集与统计 |
| **腾讯地图** | 定位、地图展示 |
| **微信开放平台** | 微信登录、分享、支付 |
| **QQ 互联** | QQ 登录、分享 |
| **MobSDK / ShareSDK** | 社交分享 |
| **友盟统计** | 用户行为统计分析 |
| **百度语音** | 语音识别 |
| **OpenInstall** | 带参数安装、上下级邀请关系建立 |
| **美狐 MHSDK** | 美颜、滤镜、特效 |
| **七牛云存储** | 文件/图片/视频上传存储 |

---

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 11+
- Gradle 8.5

### 构建步骤

1. 克隆项目并导入 Android Studio

2. 配置 `config.gradle` 中的第三方密钥（请替换为您在对应平台申请的应用凭证）:

   ```groovy
   manifestPlaceholders = [
       serverHost       : "https://your-api-server.com",
       TxIMAppId        : "您的腾讯IM AppId",
       WxAppId          : "您的微信AppId",
       QQAppId          : "您的QQ AppId",
       // ... 其他密钥
   ]
   ```

3. 使用 Gradle Wrapper 构建:

   ```bash
   ./gradlew assembleDebug
   ```

4. 生成的 APK 位于 `app/build/outputs/apk/debug/`

---

## 构建配置说明

- **签名文件**: `yunbao.jks`（alias: `phonelive`, password: `phonelive`）
- **debug 构建**默认使用 release 签名配置
- 打包 APK 时自动重命名格式: `直播demo_v{versionName}_{yyMMddHHmm}.apk`

如需生成正式发布包:

```bash
./gradlew assembleRelease
```

---

## 贡献

如果您有任何改进建议或 Bug 反馈，欢迎提交 Issue 或 Pull Request。

---

## 许可

本项目为内部项目，未经授权不得用于商业用途。