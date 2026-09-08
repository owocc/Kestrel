# Kestrel

<p align="center">
  <img src="media/banner.jpeg" alt="Kestrel Banner" style="width: 100%; max-width: 800px; border-radius: 16px;" />
</p>

<p align="center">
  <strong>专为 AI 编程与个人 Agent 办公打造的极简移动远程终端</strong>
</p>

<p align="center">
  <a href="https://github.com/owocc/Kestrel"><img src="https://img.shields.io/badge/Release-v1.0.0--alpha-blue.svg?style=flat-square" alt="Version" /></a>
  <a href="https://developer.android.com/about/versions/16"><img src="https://img.shields.io/badge/Android-16%20Ready%20(API%2036)-brightgreen.svg?style=flat-square" alt="Android 16" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-orange.svg?style=flat-square" alt="License" /></a>
  <a href="https://github.com/owocc/Kestrel"><img src="https://img.shields.io/badge/Vibe%20Coding-100%25-ff69b4.svg?style=flat-square" alt="Vibe" /></a>
</p>

---

## 🌟 什么是 Kestrel？

**Kestrel**（红隼）是一款专为 **AI 编程** 与 **个人 Agent 办公** 深度开发的现代移动远程终端。

不同于传统粗糙笨重的 SSH 移动工具，Kestrel 深度融合了 **OpenAI / ChatGPT 极简交互美学** 与 **先进的多 Agent 结构化工作流规范**。它专为在移动设备上无缝调度、掌控远程服务器中的各类 Coding Agent（如 `omp`、`Claude Code`、`Codex`、`Aider` 等）与日常个人自动化工作流而生，同时保留了完整强大的交互式 VT100 / ANSI 原生终端能力。

---

## ✨ 核心特色功能

### 1. 🌓 真正的沉浸式双模工作区 (Work <-> Terminal)
- **Work（AI 智能协同模式）**：
  - **ChatGPT 风格一体化输入胶囊**：悬浮抬高设计，集成多行拓展、文件插入与操作动作。
  - **AI 纯净直接输出**：彻底去除陈旧的聊天气泡与冗余小头像，Markdown、代码块与结构化步骤自然展开，视野开阔。
  - **全量文本选择支持**：内置 `SelectionContainer` 与针对深/浅模式定制的高对比度半透明选区遮罩（带细腻灰色水滴手柄），任意段落长按即选、随意复制。
  - **默认折叠执行流**：多步骤 Agent 运行详情默认收纳为轻量触发条，零边距平铺，清爽克制。
- **Terminal（原生交互终端模式）**：
  - 高性能真实 PTY / Shell 会话，支持完整 ANSI 颜色与特殊快捷键栏。
  - 展开式多行脚本编辑器，自适应软键盘抬升，自动聚焦闪烁光标，命令编辑丝滑流畅。
- **中央双模切换胶囊**：顶栏搭载触感弹簧滑块，一键在对话工作流与物理命令行之间瞬间无缝穿梭。

### 2. 🗂️ 首页服务器卡片系统与隐私保护
- **OpenAI 风格智能圆角与间距**：列表首尾卡片自适应 `22dp / 6dp` 大小圆角变换，中间保持 3dp 呼吸空隙。
- **正方形网格（1:1）与左上角排版**：双列网格模式下严格呈正方形比例，标题与用途描述自左上角自然铺陈。
- **25° 反向倾斜浮雕暗纹**：卡片底纹内嵌 -25° 逆时针微光矢量图标，向右自然渐隐至透明（5% 极致克制透明度），绝不干扰前排文字阅读。
- **全方位隐私安全保护**：卡片上彻底隐去敏感的主机 IP、端口与用户名，仅显示自定义服务器标题与用途描述。
- **本地偏好持久化**：随心切换列表或网格排列，自动记住你的偏好视图。

### 3. 🤖 深度多 Agent 运行时生态
- **支持自选丰富图标库**：内置经过精心分类整理的官方高品质 **Tabler Icons** 矢量图集，包含终端、服务器、云、Docker、Git 等精品图标。
- **一键 SSH 快速探针**：服务器 Agent 配置页配备右上角动态旋转刷新按钮与快速检测卡片，通过无侵入式探针深度嗅探远程主机上的 Agent 运行环境。
- **独立规范化单选弹窗**：切换 Agent、切换 LLM 模型（如 `gemini-3.8-flash`、`claude-sonnet`、`gpt-4o`、`deepseek-chat`）及调整思考程度（Thinking Level）均采用固定 2/3 屏幕高度、带有顶部渐隐微光圆角描边（`bottomSheetTopBorder`）与 ChatGPT 同心圆单选卡片。

### 4. 📜 全新独立双模式“日志”页面
- **纯净顶栏切换**：页头右侧一个圆形操作按钮即可在 **`结构化事件流`** 与 **`纯文本原始日志`** 之间秒切。
- **结构化模式**：一行一个事件卡片，包含精确时间戳、彩色状态类型小胶囊与可折叠的 JSON/调用参数。
- **纯文本模式**：深色 Monospace 终端文本框，支持横向 + 纵向平滑滚动及长按自由划词。

### 5. 🎯 现代手势流与沉浸式体验
- **全屏预测式返回手势（Predictive Back）**：
  - 基于 `SeekableTransitionState` 构建的全屏单 Activity 纯 Compose 导航栈（`screenStack`）。
  - 支持边缘右滑返回手势实时跟随手指进度缩放与预览上一级页面，如丝般顺滑。
- **纯粹黑白高对比度主题**：
  - 纯黑（`#000000`）与纯白（`#F8F9FB`）底色，搭配与顶栏滑块严格一致的高级中灰强调色（`#424242` / `#FFFFFF`）。

---

## 🛠️ 技术栈与架构

- **核心语言**：Kotlin 2.0+ (Jetpack Compose 现代声明式 UI)
- **编译目标**：Android 16 (API 36 / Android VanillaIceCream)
- **通信协议**：JSch (SSH2 Client / PTY Shell / Exec Channel)
- **多智能体协议**：Multica Agent Event Stream / OMP JSON Protocol
- **矢量图标系统**：
  - **Tabler Icons Compose** (官方原生 ImageVector 适配)
  - **Lucide Icons** (高精度 24x24 原版几何路径)
- **代码字体**：JetBrains Mono Nerd Font / Fira Code Nerd Font
- **本地存储**：AndroidX SharedPreferences / SQLite LibSQL 本地数据库

---

## 🚀 编译与快速开始

### 依赖环境
- Android Studio Ladybug | 2024.2.1 或更新版本
- JDK 17 / 21
- Android SDK Platform 36 (Android 16)

### 本地编译构建
```bash
# 克隆本仓库
git clone git@github.com:owocc/Kestrel.git
cd Kestrel

# 使用 Gradle 编译 Debug APK
./gradlew assembleDebug

# 安装到连接的设备
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 💖 特别致谢 (Acknowledgements)

Kestrel 是一次纯粹而热烈的 **Vibe Coding** 探索，在此诚挚致谢：

- **哈吉米 (Gemini)**：随叫随到的 AI 结对编程搭子，负责秒懂每一次奇思妙想，把每一处灵感利索敲成优雅代码！
- **omp (Oh My Pi)**：极其好用、高效敏捷的 AI Coding Agent，驱动整个开发流程顺畅流转的核心加速器。
- **Multica**：感谢 Multica 优秀的多 Agent 交互架构，为本项目提供了至关重要的灵感启发与坚实参考。
- **OpenAI / ChatGPT**：致敬 OpenAI 移动端出色的交互节奏与现代美学设计，带来极致克制而高级的视觉灵感。

---

## 📄 开源许可证

本项目基于 **[Apache License 2.0](LICENSE)** 协议开源。详细第三方依赖库、字体与矢量图集授权可进入 App 内的 **设置 -> 关于 -> 打开开源许可证** 查看。
