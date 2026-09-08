package com.bettershell.app.ui.components

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.*

/**
 * 服务器预设图标库元数据
 */
data class ServerIconOption(
    val key: String,
    val name: String,
    val category: String,
    val icon: ImageVector
)

/**
 * 集中管理 Tabler Icons 服务器图标库
 * 包含开发、云与基础设施、系统与设备、网络与安全、运维与常用等各领域的精品图标
 */
object ServerIconCatalog {

    // 默认图标 key
    const val DEFAULT_KEY = "terminal"

    val ALL_ICONS: List<ServerIconOption> = listOf(
        // 1. 终端与代码 (Terminal & Code)
        ServerIconOption("terminal", "终端", "终端与代码", TablerIcons.Terminal),
        ServerIconOption("terminal_alt", "极简终端", "终端与代码", TablerIcons.Terminal2),
        ServerIconOption("prompt", "命令行提示", "终端与代码", TablerIcons.Prompt),
        ServerIconOption("code", "代码", "终端与代码", TablerIcons.Code),
        ServerIconOption("file_code", "代码文件", "终端与代码", TablerIcons.FileCode),
        ServerIconOption("command", "指令", "终端与代码", TablerIcons.Command),
        ServerIconOption("git_branch", "Git 分支", "终端与代码", TablerIcons.GitBranch),
        ServerIconOption("git_commit", "Git 提交", "终端与代码", TablerIcons.GitCommit),
        ServerIconOption("git_pr", "Pull Request", "终端与代码", TablerIcons.GitPullRequest),
        ServerIconOption("binary", "二进制", "终端与代码", TablerIcons.Binary),

        // 2. 服务器与基础设施 (Server & Cloud)
        ServerIconOption("server", "机架服务器", "服务器与云", TablerIcons.Server),
        ServerIconOption("cloud", "云端", "服务器与云", TablerIcons.Cloud),
        ServerIconOption("cloud_upload", "云端部署", "服务器与云", TablerIcons.CloudUpload),
        ServerIconOption("cloud_download", "云端拉取", "服务器与云", TablerIcons.CloudDownload),
        ServerIconOption("database", "数据库", "服务器与云", TablerIcons.Database),
        ServerIconOption("db_import", "数据库导入", "服务器与云", TablerIcons.DatabaseImport),
        ServerIconOption("db_export", "数据库导出", "服务器与云", TablerIcons.DatabaseExport),
        ServerIconOption("stack", "技术栈", "服务器与云", TablerIcons.Stack),
        ServerIconOption("stack_alt", "双层技术栈", "服务器与云", TablerIcons.Stack2),
        ServerIconOption("package", "软件包", "服务器与云", TablerIcons.Package),
        ServerIconOption("box", "容器盒", "服务器与云", TablerIcons.Box),
        ServerIconOption("archive", "压缩归档", "服务器与云", TablerIcons.Archive),
        ServerIconOption("layers", "图层架构", "服务器与云", TablerIcons.LayersIntersect),

        // 3. 设备与硬件 (Devices & Hardware)
        ServerIconOption("desktop", "台式机", "设备与硬件", TablerIcons.DeviceDesktop),
        ServerIconOption("laptop", "笔记本", "设备与硬件", TablerIcons.DeviceLaptop),
        ServerIconOption("pc_cluster", "PC 集群", "设备与硬件", TablerIcons.DevicesPc),
        ServerIconOption("devices", "多设备协同", "设备与硬件", TablerIcons.Devices),
        ServerIconOption("mobile", "移动设备", "设备与硬件", TablerIcons.DeviceMobile),
        ServerIconOption("router", "路由器", "设备与硬件", TablerIcons.Router),
        ServerIconOption("wifi", "Wi-Fi 无线", "设备与硬件", TablerIcons.Wifi),
        ServerIconOption("cast", "广播投射", "设备与硬件", TablerIcons.Cast),
        ServerIconOption("satellite", "卫星基站", "设备与硬件", TablerIcons.Satellite),

        // 4. 生态与开源技术 (Brands & Tech)
        ServerIconOption("docker", "Docker 容器", "品牌与生态", TablerIcons.BrandDocker),
        ServerIconOption("github", "GitHub", "品牌与生态", TablerIcons.BrandGithub),
        ServerIconOption("gitlab", "GitLab", "品牌与生态", TablerIcons.BrandGitlab),
        ServerIconOption("open_source", "开源倡议", "品牌与生态", TablerIcons.BrandOpenSource),
        ServerIconOption("python", "Python", "品牌与生态", TablerIcons.BrandPython),
        ServerIconOption("kotlin", "Kotlin", "品牌与生态", TablerIcons.BrandKotlin),
        ServerIconOption("apple", "Apple macOS", "品牌与生态", TablerIcons.BrandApple),
        ServerIconOption("android", "Android", "品牌与生态", TablerIcons.BrandAndroid),
        ServerIconOption("windows", "Windows", "品牌与生态", TablerIcons.BrandWindows),

        // 5. 安全与运维状态 (Security & Monitoring)
        ServerIconOption("shield", "安全防护", "安全与运维", TablerIcons.Shield),
        ServerIconOption("shield_check", "安全合规", "安全与运维", TablerIcons.ShieldCheck),
        ServerIconOption("lock", "访问锁", "安全与运维", TablerIcons.Lock),
        ServerIconOption("key", "SSH 密钥", "安全与运维", TablerIcons.Key),
        ServerIconOption("activity", "实时监控", "安全与运维", TablerIcons.Activity),
        ServerIconOption("dashboard", "仪表盘", "安全与运维", TablerIcons.Dashboard),
        ServerIconOption("bolt", "高性能雷电", "安全与运维", TablerIcons.Bolt),
        ServerIconOption("flame", "算力烈焰", "安全与运维", TablerIcons.Flame),
        ServerIconOption("rocket", "火箭部署", "安全与运维", TablerIcons.Rocket),
        ServerIconOption("tools", "运维工具", "安全与运维", TablerIcons.Tools),
        ServerIconOption("globe", "公网节点", "安全与运维", TablerIcons.Globe),
        ServerIconOption("infinity", "CI/CD 无限流水线", "安全与运维", TablerIcons.Infinity),
        ServerIconOption("atom", "微服务原子", "安全与运维", TablerIcons.Atom),
        ServerIconOption("alien", "智能异构", "安全与运维", TablerIcons.Alien),
        ServerIconOption("bug", "测试调试", "安全与运维", TablerIcons.Bug)
    )

    private val iconMap: Map<String, ImageVector> = ALL_ICONS.associate { it.key to it.icon }

    /**
     * 根据 key 查找对应图标，找不到则智能回退
     */
    fun getIcon(key: String, isMock: Boolean = false): ImageVector {
        if (key.isNotBlank()) {
            iconMap[key]?.let { return it }
        }
        return if (isMock) TablerIcons.Alien else TablerIcons.Terminal
    }
}
