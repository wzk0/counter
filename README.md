# Counter

一个 Material 3 Expressive 风格的多计数器 Android App。原生 Compose 实现，跟随系统 Monet 动态取色。

## 功能

- **多个计数器同时进行**，互不影响；宽松 / 紧凑两种列表样式可切换
- **Monet 取色**：Android 12+ 动态取色；深色模式跟随系统
- **手势**：右滑置顶、左滑删除；长按 `−` / `+` 连加
- 置顶 / 未置顶分区展示，AppBar 副标题实时显示置顶计数和、总计数和与数量
- 新建自动保留输入历史，SharedPreferences 持久化数据
- 一键全部归零、全部删除；支持重命名、单条归零

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

- `app/build/outputs/apk/debug/app-debug.apk`（约 13 MB，未压缩）
- `app/build/outputs/apk/release/app-release.apk`（约 1.3 MB：release 开启 R8 代码压缩 + 资源压缩）

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