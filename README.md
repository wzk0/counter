# Counter

一个 Material 3 Expressive 风格的多计数器 Android App。原生 Compose 实现，跟随系统 Monet 动态取色。

## 功能

- **多个计数器同时进行**，互不影响；宽松 / 紧凑两种列表样式可切换（紧凑样式把 `−` `+` 收进同一行行尾）
- **Monet 取色**：Android 12+ 直接使用系统壁纸动态色板，低版本回退到 Expressive 基线配色；深色模式跟随系统
- **手势**：右滑置顶（再次右滑取消）、左滑删除，都有方向对应的背景提示；长按 `−` / `+` 连加
- 置顶 / 未置顶分区展示，AppBar 副标题实时显示置顶计数和、总计数和与数量
- 新建时保留输入历史，数据用 SharedPreferences 持久化，杀进程不丢
- 全部归零、全部删除（带确认）、重命名、单条归零

## 环境

- JDK 17
- Android SDK Platform 36（`compileSdk = 36`）
- Gradle 由仓库自带的 wrapper 提供

## 构建

```bash
./gradlew :app:assembleDebug      # 调试包
./gradlew :app:assembleRelease    # 已配置签名的正式包
```

产物：

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

## 签名

正式包使用仓库根目录的 `counter-release.jks`，凭据放在根目录 `keystore.properties`（两者都已被 `.gitignore` 排除，请自行备份，丢失将无法覆盖升级）：

```properties
storeFile=counter-release.jks
storePassword=******
keyAlias=counter
keyPassword=******
```

也可以用 `-P` 覆盖同名属性，例如 CI 环境。缺少任一凭据时 release 构建会产出未签名包而不会失败。

## 发布

推一个 `v*` 标签即可触发 GitHub Actions 构建并创建 Release：

```bash
git tag v1.0.0
git push origin v1.0.0
```

工作流需要的仓库 secrets：

| Secret | 内容 |
| --- | --- |
| `KEYSTORE_BASE64` | `base64 -w0 counter-release.jks` 的输出 |
| `KEYSTORE_PASSWORD` | keystore 密码 |
| `KEY_ALIAS` | key 别名（当前为 `counter`） |
| `KEY_PASSWORD` | key 密码 |

CI 会把标签去掉 `v` 作为 `versionName`，用运行序号作为 `versionCode`，并把签名后的 APK 附到 Release 上。

## 项目结构

```
app/src/main/kotlin/com/example/counter/
├── MainActivity.kt            # 入口，注入 ViewModel
├── data/CounterItem.kt        # 数据模型 + SharedPreferences/JSON 存储
└── ui/
    ├── CounterScreen.kt       # 主界面：AppBar、列表分区、滑动、弹层
    ├── CounterCard.kt         # 宽松 / 紧凑两种计数器卡片与步进按钮
    ├── CounterViewModel.kt    # 状态、持久化、历史记录
    ├── Glyphs.kt              # Canvas 绘制的图标（无额外图标依赖）
    └── theme/Theme.kt         # Monet 动态色 + Expressive 主题
```