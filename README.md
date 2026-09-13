# Kestrel

<p align="center">
  <img src="media/banner.jpeg" alt="Kestrel Banner" style="width: 100%; max-width: 800px; border-radius: 16px;" />
</p>

<p align="center">
  <strong>专为远程控制电脑与服务器所有 AI Coding Agent 打造的极简移动 SSH 终端</strong>
</p>

<p align="center">
  <a href="https://github.com/owocc/Kestrel"><img src="https://img.shields.io/badge/Release-v1.0.0--alpha-blue.svg?style=flat-square" alt="Version" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-orange.svg?style=flat-square" alt="License" /></a>
  <a href="https://github.com/owocc/Kestrel"><img src="https://img.shields.io/badge/Vibe%20Coding-100%25-ff69b4.svg?style=flat-square" alt="Vibe" /></a>
</p>

---

## 🌟 什么是 Kestrel？

**Kestrel**（红隼）是一款专注于 **远程 SSH 连接、一键调度电脑与服务器上所有 AI Agent 并提供完备终端操作** 的现代移动生产力工具。

出门在外或躺在沙发上，无需随身携带厚重的笔记本电脑。只需在手机上打开 Kestrel，就能通过安全快速的 SSH 协议直连你的远程主机、工作站或云服务器：
- **一键感知与唤起电脑上的所有 Coding Agent**：不论是 `omp`、`Claude Code`、`Codex`、`Aider`、`OpenCode` 还是自定义 CLI 工具，全部无缝纳管；
- **纯粹强大的原生交互终端**：提供完整 PTY Shell、ANSI 彩色高亮与长文本编辑支持，满足你随时随地查看进程、修改配置、运行脚本的全部终端运维诉求。

---

## ✨ 核心主打功能

### 1. ⚡ 远程 SSH 一键调用电脑上的所有 AI Agent
- **无侵入式智能环境探针**：连接远程服务器后，后台探针会自动扫描登录 Shell、系统 PATH 以及常用环境管理器（`nvm`、`fnm`、`cargo`、`~/.local/bin` 等），深度感知并汇总当前电脑上已安装的全部 AI 智能体；
- **全方位 Agent 随心切换**：支持在会话中一键自由挑选不同的 Agent 工具（如用 `omp` 进行全栈代码生成、用 `Claude Code` 快速审查、用 `Codex` 辅助调优）；
- **动态大模型与思考程度配置**：无需在命令行记忆复杂参数，图形化一键切换推理大模型（`gemini-3.8-flash`、`claude-sonnet`、`gpt-4o`、`deepseek-chat` 等）并按需调控思考程度（Thinking Level）；
- **原生多 Agent 结构化工作流**：支持解析并呈现 Agent 的思考链（Thinking）、多步工具调用（Tool Use）与结构化执行日志，随时把控执行进度。

### 2. 💻 完备强大的移动端交互式原生终端
- **真正全功能的 PTY 终端**：内置高性能 VT100 / ANSI 终端仿真引擎，完美支持 `vim`、`tmux`、`htop` 等全屏终端应用的流畅交互与彩色渲染；
- **专业命令行快捷输入栏**：精心配备 `Ctrl`、`Esc`、`Tab`、方向键以及管道符等移动端不可或缺的物理按键辅助栏；
- **多行脚本展开编辑器**：支持一键将单行输入展开为全尺寸长文本代码编辑器，软键盘自动升起聚焦，编写多行复杂 Shell 脚本或长 Prompt 轻松从容。

### 3. 🌓 对话工作流与终端模式一键切换
- **中央双模滑块（Work <-> Terminal）**：
  - **Work 模式**：专注于与 AI Agent 的对话与结构化产出，文字与代码直接平铺排版，支持任意文本长按自由选择与复制；
  - **Terminal 模式**：专注于 Linux 原生命令交互与系统运维；
  - 顶栏一键极速横跳，两套运行时状态独立保持、互不干扰。

### 4. 🗂️ 服务器多节点资产管理与隐私保护
- **多主机快速接入**：支持密码认证、私钥免密登录（PEM / OpenSSH / 带 Passphrase 私钥）；
- **自选图标个性化标识**：内置丰富的官方 **Tabler Icons** 系统图标库（终端、服务器、Docker、Git、云等），随心定制每个节点的专属外观；
- **隐私保护设计**：首页卡片严格隐藏真实主机 IP 与账号端口，仅展示自定义服务器名称与业务用途说明。

### 5. 🔀 SSH 端口映射（本地转发）
- **一键把服务器服务搬到手机本地**：在服务器卡片的「更多」菜单选择「端口映射」，输入远程端口与本地端口，即可用该服务器的密码或私钥建立 SSH 本地转发，手机浏览器直接访问 `127.0.0.1:本地端口` 就能打开服务器上的 Web / API / 数据库面板；
- **后台持续存活**：隧道由前台服务保活，切到浏览器或其它 App 也不会中断，通知栏可随时查看状态并「全部停止」；
- **多隧道并存**：同一服务器可映射多个端口（复用一条 SSH 连接），不同服务器各自独立、互不影响；
- **本地端口自动分配**：本地端口留空即由系统分配，界面会回填真实可用端口并支持一键复制。

---

## 🛠️ 技术栈

- **构建框架**：Kotlin / Jetpack Compose 现代声明式 UI
- **SSH 引擎**：JSch (SSH2 纯 Java 协议栈 / PTY Shell / Exec Channel)
- **多智能体规范**：OMP JSON 协议 / Multica 结构化事件流驱动
- **图标系统**：Tabler Icons Compose / Lucide Icons
- **本地存储**：SQLite LibSQL / AndroidX SharedPreferences

---

## 🚀 快速开始

### 本地编译构建
```bash
# 1. 克隆代码仓库
git clone git@github.com:owocc/Kestrel.git
cd Kestrel

# 2. 编译 Debug APK
./gradlew assembleDebug

# 3. 安装到手机 (确保开启 ADB 调试)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 💖 特别致谢 (Acknowledgements)

- **哈吉米 (Gemini)**：随叫随到的 AI 结对编程搭子，负责秒懂你的奇思妙想，把每一处灵感利索敲成代码！
- **omp (Oh My Pi)**：极其好用、高效敏捷的 AI Coding Agent，驱动整个开发流程顺畅流转的核心加速器。
- **Multica**：感谢 Multica 优秀的多 Agent 交互架构，为本项目提供了至关重要的灵感启发与坚实参考。
- **OpenAI**：致敬 OpenAI 移动端出色的交互节奏与现代美学设计，带来极致克制而高级的视觉灵感。

---

## 📄 开源协议

本项目基于 **[Apache License 2.0](LICENSE)** 协议开源。详细第三方依赖库与图标授权可在 App 内的 **设置 -> 关于 -> 打开开源许可证** 中查阅。
