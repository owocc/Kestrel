package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bettershell.app.AppConstants
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.OpenAiSectionCard
import com.bettershell.app.ui.components.OpenAiSettingRow
import com.bettershell.app.ui.components.StandardPageHeader
import com.bettershell.app.ui.theme.isAppInDarkTheme

data class OpenSourceLibrary(
    val name: String,
    val description: String,
    val license: String
)

/**
 * 开源许可证与组件来源页面 (OpenSourceLicensesScreen)
 * 完整对标 ChatGPT 独立分段色块卡片风格，展示项目核心协议、所有依赖库及图标资源来源
 */
@Composable
fun OpenSourceLicensesScreen(
    onBack: () -> Unit
) {
    val isDark = isAppInDarkTheme

    val coreProjectLicenses = listOf(
        OpenSourceLibrary(
            name = AppConstants.APP_NAME,
            description = "Android 16 现代极简远程 AI Agent 终端与工作流平台 (本项目全部开源)",
            license = "Apache License 2.0"
        ),
        OpenSourceLibrary(
            name = "Multica & OMP Protocol",
            description = "多智能体架构与 IDE 级 Coding Agent 远程通信协议规范",
            license = "Apache License 2.0"
        )
    )

    val iconAndDesignLibraries = listOf(
        OpenSourceLibrary(
            name = "Lucide Icons",
            description = "极简高精度 24x24 矢量线条图标库",
            license = "ISC License"
        ),
        OpenSourceLibrary(
            name = "Tabler Icons",
            description = "5000+ 精致高保真开源 SVG 矢量系统图标库",
            license = "MIT License"
        ),
        OpenSourceLibrary(
            name = "Jetpack Compose Icons",
            description = "DevSrSouza 提供的 Compose 原生 ImageVector 适配方案",
            license = "Apache License 2.0"
        ),
        OpenSourceLibrary(
            name = "Jetpack Compose Material Icons Extended",
            description = "Google Material 扩展图标系统",
            license = "Apache License 2.0"
        )
    )

    val androidLibraries = listOf(
        OpenSourceLibrary(
            name = "Android Jetpack & Compose BOM",
            description = "Google 现代声明式 UI 工具包与 Material Design 3 体系",
            license = "Apache License 2.0"
        ),
        OpenSourceLibrary(
            name = "Kotlin Coroutines & Serialization",
            description = "JetBrains 异步协程与现代化类型安全 JSON 序列化组件",
            license = "Apache License 2.0"
        ),
        OpenSourceLibrary(
            name = "JSch (mwiede fork)",
            description = "纯 Java 实现的高性能 SSH2 客户端通信协议栈",
            license = "BSD-style License"
        ),
        OpenSourceLibrary(
            name = "JetBrains Mono & Fira Code Nerd Font",
            description = "专业等宽代码编程与终端修饰连字字体",
            license = "OFL 1.1 / Apache License 2.0"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        StandardPageHeader(
            title = "开源许可证",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. 本项目开源声明
            OpenAiSectionCard(
                headerTitle = "项目协议"
            ) {
                coreProjectLicenses.forEachIndexed { index, lib ->
                    val position = when {
                        coreProjectLicenses.size == 1 -> CardPosition.SINGLE
                        index == 0 -> CardPosition.TOP
                        index == coreProjectLicenses.size - 1 -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }
                    OpenAiSettingRow(
                        title = lib.name,
                        subtitle = lib.description,
                        trailingText = lib.license,
                        position = position,
                        isDark = isDark
                    )
                }
            }

            // 2. 图标与视觉资产来源
            OpenAiSectionCard(
                headerTitle = "图标与视觉资源"
            ) {
                iconAndDesignLibraries.forEachIndexed { index, lib ->
                    val position = when {
                        iconAndDesignLibraries.size == 1 -> CardPosition.SINGLE
                        index == 0 -> CardPosition.TOP
                        index == iconAndDesignLibraries.size - 1 -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }
                    OpenAiSettingRow(
                        title = lib.name,
                        subtitle = lib.description,
                        trailingText = lib.license,
                        position = position,
                        isDark = isDark
                    )
                }
            }

            // 3. 核心框架与通信类库
            OpenAiSectionCard(
                headerTitle = "核心依赖与框架"
            ) {
                androidLibraries.forEachIndexed { index, lib ->
                    val position = when {
                        androidLibraries.size == 1 -> CardPosition.SINGLE
                        index == 0 -> CardPosition.TOP
                        index == androidLibraries.size - 1 -> CardPosition.BOTTOM
                        else -> CardPosition.MIDDLE
                    }
                    OpenAiSettingRow(
                        title = lib.name,
                        subtitle = lib.description,
                        trailingText = lib.license,
                        position = position,
                        isDark = isDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
