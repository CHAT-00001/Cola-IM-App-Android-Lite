# 可乐 IM (Cola-IM) SDK 核心开发规范

你是一个资深的 Android/Kotlin 架构师与音视频通讯专家。请严格按照以下技术规范与设计模式，进行 Cola-Im-SDK 模块的落地实现。

## 一、项目模块与架构边界
### 1. 模块划分
在项目根目录建立独立库模块 `cola-im-sdk`，内部通过包结构实现强内聚、低耦合划分：
- `com.cola.im.sdk.core`：对外 Facade 门面、SDK 初始化、全局状态机、MVI 架构的 Intent/State 控制中心
- `com.cola.im.sdk.network`：多介质连接管理器、长连接（WebSocket/Socket）、端口跳跃、自适应心跳
- `com.cola.im.sdk.storage`：Room 数据库、单用户单库路由锁、SQLCipher 加密、文件及媒体缓存
- `com.cola.im.sdk.crypto`：Signal 协议（X3DH + Double Ratchet）端到端加解密实现

### 2. 架构模式：严格 MVI (Model-View-Intent)
1. 禁止在 UI 层直接操作数据库或发起网络请求
2. 唯一真实数据源（SSOT）：UI 层通过 Compose `collectAsStateWithLifecycle()` 订阅存储层暴露的 Kotlin `Flow<List<MessageEntity>>`
3. 数据流向：
   UI 触发 Intent → CoreEngine 处理 → 更新 Storage → Storage 驱动 Flow 生成新 State → UI 自动重绘

## 二、消息全生命周期与“永不丢失”双层 ACK 状态机
每条消息严格执行状态流转，本地数据库实时同步状态，状态枚举：`MessageStatus: SENDING, SUCCESS, FAILED, READ`

### 1. 上行状态机（发送端 → 服务器）
1. **乐观写入**：UI 触发发送，SDK 生成 UUID v4 全局唯一 `send_id`，落库并标记状态为 `SENDING`，通过 Flow 驱动 UI 渲染
2. **连接验证**：校验长连接是否为 `AUTHENTICATED` 已认证状态；离线则加入断线重发指数退避队列，**2000ms** 投递超时后状态改为 `FAILED`，并抛出重发事件
3. **上行确权**：发送消息二进制流，等待服务端返回 `Server_ACK`（包含 `sync_id`、`seq_no`、`sync_at`）
4. **状态更新**：收到 `Server_ACK` 后，原子更新本地消息状态为 `SUCCESS`，补全 `sync_id`、`seq_no`

### 2. 下行状态机（服务器 → 接收端多设备）
1. **断线空洞检测**：客户端上线/网络切换后，上报各通道本地最大 `seq_no`；服务端对比后，流式增量下发缺失消息
2. **幂等去重落库**：通过 `sync_id` 或 `send_id + from_uid` 查重；消息已存在则丢弃并补发下行 ACK，不存在则原子落库并回传 `Client_ACK(sync_id)`
3. **视觉可见已读（Read_ACK）**：Compose `LazyColumn` + `LaunchedEffect` 监听消息条目，消息在可视区域停留超 **1000ms** 触发 `ReadReport(msgIds)`；本地状态更新为 `READ`，并批量上报 `Read_ACK`

## 三、智能自适应心跳与网络防御规范
网络层需具备抗封锁、低功耗、多介质适配能力。

### 1. 介质与心跳策略
长连接区分场景使用双模心跳：
- **前台硬连模式（USB / LAN / 蓝牙）**：固定 **10s** 高频心跳，快速感知断线
- **后台蜂窝模式（4G / 5G / WIFI）**：自适应递增心跳算法
    - 初始间隔：60s
    - 递增规则：连续成功 3 次，间隔 +30s
    - 最大上限：270s（4.5分钟）
    - 失败回退：同一间隔连续 2 次超时，安全上限 = 当前间隔 - 30s，重连后锁定该周期
    - 重置条件：网络切换（WIFI ↔ 5G）时重置心跳算法

### 2. 端口跳跃 (Port Hopping)
基于 `CurrentDate + DeviceID` 生成伪随机端口序列；长连接异常断开，原端口重连 **3 次失败** 后，自动切换下一端口重连，规避端口封锁。

## 四、存储层架构与单用户单库隔离
### 1. 数据库隔离
禁止多用户共用数据库。用户登录鉴权后，根据 `uid` 的 SHA-256 值创建/打开独立库文件：
`cola_im_{sha256(uid)}.db`
账号登出/切换时，必须显式调用 `.close()` 释放数据库句柄与缓存锁。

### 2. 安全与性能
- 加密预留：基于 Room 保留 `SupportSQLiteOpenHelper`，兼容后续 SQLCipher 动态密钥加密
- 性能优化：强制开启 SQLite WAL 日志模式；批量消息落库必须使用 `@Transaction` 事务，禁止单条循环插入

## 五、端到端对等加密 (E2EE) 规范
消息三层封装结构：**信封层(Envelope) → 消息体(Message) → 负载(Payload)**
1. **信封层（不加密）**：存放 `send_id`、`sync_id`、`from_uid`、`to_target_id`、`channel` 等路由字段，供服务端转发
2. **内容全加密**：`is_encrypted = true` 时，使用 Signal 协议（X3DH 密钥协商 + Double Ratchet 双棘轮）加密完整 `MsgBody`；密文存入 `payload`，棘轮公钥参数存入 `crypto_noise`，服务端无法解密消息内容

## 六、协议契约与超级消息类型定义 (Protobuf)
采用 Protobuf V3 定义核心传输协议，使用 `oneof` 特性保证媒体类型可无限扩展，解析与序列化严格遵循协议结构。