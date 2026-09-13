package com.bettershell.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.data.ServerRepository
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.portforward.PortForwardManager
import com.bettershell.app.ui.components.CardPosition
import com.bettershell.app.ui.components.LucideIcons
import com.bettershell.app.ui.components.OpenAiDropdownMenu
import com.bettershell.app.ui.components.OpenAiMenuItemData
import com.bettershell.app.ui.components.PortForwardDialog
import com.bettershell.app.ui.components.getCardShape
import com.bettershell.app.ui.theme.AccentGreen
import com.bettershell.app.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch

enum class ServerLayoutMode {
    LIST,
    GRID
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    repository: ServerRepository,
    prefsRepository: TerminalPreferencesRepository,
    portForwardManager: PortForwardManager,
    onOpenAppSettings: () -> Unit,
    onSelectServer: (ServerConfig) -> Unit,
    onOpenAddServer: () -> Unit,
    onOpenServerSettings: (ServerConfig) -> Unit
) {
    val isDark = isAppInDarkTheme
    val servers by repository.servers.collectAsState()
    val prefs by prefsRepository.preferences.collectAsState()
    val layoutMode = if (prefs.serverLayoutMode == "GRID") ServerLayoutMode.GRID else ServerLayoutMode.LIST
    var searchQuery by remember { mutableStateOf("") }

    // 端口映射入口：记录当前要打开映射对话框的服务器
    var portForwardTarget by remember { mutableStateOf<ServerConfig?>(null) }
    val portForwardTunnels by portForwardManager.tunnels.collectAsState()
    val activeForwardCountByServer = remember(portForwardTunnels) {
        portForwardTunnels.filter { it.isLive }.groupingBy { it.serverId }.eachCount()
    }

    val scope = rememberCoroutineScope()
    val filteredServers = remember(servers, searchQuery) {
        if (searchQuery.isBlank()) servers
        else servers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = com.bettershell.app.AppConstants.APP_NAME,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // 网格与列表模式切换：纯图标，无背景，位于设置左侧
                    IconButton(
                        onClick = {
                            val newMode = if (layoutMode == ServerLayoutMode.LIST) "GRID" else "LIST"
                            prefsRepository.updateServerLayoutMode(newMode)
                        }
                    ) {
                        Icon(
                            imageVector = if (layoutMode == ServerLayoutMode.LIST) Icons.Rounded.GridView else Icons.AutoMirrored.Rounded.ViewList,
                            contentDescription = "切换视图模式",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // 右侧入口：软件设置
                    IconButton(onClick = onOpenAppSettings) {
                        Icon(
                            imageVector = LucideIcons.Bolt,
                            contentDescription = "软件设置",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAddServer,
                icon = { Icon(Icons.Rounded.Add, "添加服务器") },
                text = { Text("添加服务器", fontWeight = FontWeight.SemiBold) },
                shape = CircleShape, // 全圆角胶囊
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏：对标设置项和输入框的大圆角胶囊设计 (48dp 高度)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(48.dp),
                shape = CircleShape,
                color = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "搜索",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "搜索服务器名称或描述...",
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontSize = 14.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Default,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Normal
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "清除搜索",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 列表或空状态
            if (filteredServers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = LucideIcons.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "未找到匹配的服务器" else "暂无服务器配置",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "请尝试使用其他关键词搜索" else "点击下方按钮新建服务器",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (layoutMode == ServerLayoutMode.LIST) {
                    // 与设置中相同的卡片模式：首尾卡片上下圆角变化，中间全小圆角，间距 3dp
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp)
                    ) {
                        itemsIndexed(filteredServers, key = { _, server -> server.id }) { index, server ->
                            val position = when {
                                filteredServers.size == 1 -> CardPosition.SINGLE
                                index == 0 -> CardPosition.TOP
                                index == filteredServers.size - 1 -> CardPosition.BOTTOM
                                else -> CardPosition.MIDDLE
                            }

                            ServerCard(
                                server = server,
                                position = position,
                                isDark = isDark,
                                activeForwardCount = activeForwardCountByServer[server.id] ?: 0,
                                onConnect = { onSelectServer(server) },
                                onManage = { onOpenServerSettings(server) },
                                onOpenPortForward = { portForwardTarget = server },
                                onDelete = {
                                    scope.launch {
                                        repository.deleteServer(server.id)
                                    }
                                }
                            )

                            // 卡片间保留 3dp 纯净间隙 (同设置菜单)
                            if (index < filteredServers.size - 1) {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(filteredServers, key = { _, server -> server.id }) { _, server ->
                            ServerGridCard(
                                server = server,
                                isDark = isDark,
                                activeForwardCount = activeForwardCountByServer[server.id] ?: 0,
                                onConnect = { onSelectServer(server) },
                                onManage = { onOpenServerSettings(server) },
                                onOpenPortForward = { portForwardTarget = server },
                                onDelete = {
                                    scope.launch {
                                        repository.deleteServer(server.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // 端口映射对话框：隧道由管理器与前台服务持有，关闭对话框后继续运行
    portForwardTarget?.let { target ->
        PortForwardDialog(
            server = target,
            manager = portForwardManager,
            onDismiss = { portForwardTarget = null }
        )
    }
}

/**
 * 列表视图卡片：
 * - 采用与设置中相同卡片设计 (getCardShape 22dp/6dp，首尾圆角变化)
 * - 图标去除背景，尺寸放大，固定在最左下角，旋转 25 度
 * - 图标颜色为字体色，通过蒙版向右侧渐变至透明
 */
@Composable
fun ServerCard(
    server: ServerConfig,
    position: CardPosition,
    isDark: Boolean,
    activeForwardCount: Int = 0,
    onConnect: () -> Unit,
    onManage: () -> Unit,
    onOpenPortForward: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val titleColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val shape = getCardShape(position)
    val borderColor = if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onConnect),
        shape = shape,
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
        ) {
            // 背景层：最左边、固定在底部、旋转 25度、向右侧渐变透明的放大无背景图标
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        // 绘制向右侧渐变至透明的遮罩 (从左向右淡出)
                        val maskBrush = Brush.horizontalGradient(
                            0.0f to Color.White,
                            0.50f to Color.White.copy(alpha = 0.85f),
                            0.75f to Color.White.copy(alpha = 0.40f),
                            0.95f to Color.Transparent,
                            1.0f to Color.Transparent,
                            startX = 0f,
                            endX = size.width
                        )
                        drawRect(
                            brush = maskBrush,
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                val serverIcon = com.bettershell.app.ui.components.ServerIconCatalog.getIcon(server.icon, server.isMock)
                Icon(
                    imageVector = serverIcon,
                    contentDescription = null,
                    // 图标颜色为字体色，透明度降低到 5%，极其轻盈不干扰文字阅读
                    tint = titleColor.copy(alpha = 0.05f),
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = 10.dp, y = 6.dp)
                        .rotate(-25f) // 反方向 -25度旋转
                )
            }

            // 前景层：内容信息展示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    val descText = server.description.ifBlank { "远程连接主机" }
                    Text(
                        text = descText,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 右侧极简三点菜单
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More Options",
                            tint = subtitleColor
                        )
                    }

                    OpenAiDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        items = buildList {
                            add(
                                OpenAiMenuItemData(
                                    title = "服务器设置",
                                    icon = LucideIcons.Bolt,
                                    onClick = onManage
                                )
                            )
                            // 演示服务器没有真实网络，不提供端口映射
                            if (!server.isMock && server.authType != AuthType.DEMO_MOCK) {
                                add(
                                    OpenAiMenuItemData(
                                        title = if (activeForwardCount > 0) "端口映射 ($activeForwardCount)" else "端口映射",
                                        icon = Icons.Rounded.SwapHoriz,
                                        iconTint = if (activeForwardCount > 0) AccentGreen else Color.Unspecified,
                                        onClick = onOpenPortForward
                                    )
                                )
                            }
                            add(
                                OpenAiMenuItemData(
                                    title = "删除服务器",
                                    icon = LucideIcons.Trash2,
                                    isDestructive = true,
                                    onClick = onDelete
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 网格视图卡片：同样支持底纹大图标装饰与整卡无缝交互
 */
@Composable
fun ServerGridCard(
    server: ServerConfig,
    isDark: Boolean,
    activeForwardCount: Int = 0,
    onConnect: () -> Unit,
    onManage: () -> Unit,
    onOpenPortForward: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val titleColor = if (isDark) Color(0xFFF3F4F6) else Color(0xFF111827)
    val subtitleColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val shape = RoundedCornerShape(18.dp)
    val borderColor = if (isDark) Color(0xFF2E2E2E) else Color(0xFFE5E7EB)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 固定为完美正方形！
            .clip(shape)
            .clickable(onClick = onConnect),
        shape = shape,
        color = cardBg,
        border = BorderStroke(1.dp, borderColor), // 精致描边
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
        ) {
            // 背景层：最左边、固定在底部、旋转 25度、向右渐变透明
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithContent {
                        drawContent()
                        val maskBrush = Brush.horizontalGradient(
                            0.0f to Color.White,
                            0.50f to Color.White.copy(alpha = 0.85f),
                            0.75f to Color.White.copy(alpha = 0.40f),
                            0.95f to Color.Transparent,
                            1.0f to Color.Transparent,
                            startX = 0f,
                            endX = size.width
                        )
                        drawRect(
                            brush = maskBrush,
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                val serverIcon = com.bettershell.app.ui.components.ServerIconCatalog.getIcon(server.icon, server.isMock)
                Icon(
                    imageVector = serverIcon,
                    contentDescription = null,
                    tint = titleColor.copy(alpha = 0.05f),
                    modifier = Modifier
                        .size(86.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = 10.dp, y = 6.dp)
                        .rotate(-25f) // 反方向 -25度旋转
                )
            }

            // 内容层：从左上角开始紧凑自然排布！
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // 顶部行：左上角标题 (占据主位)，右上角极简三点菜单
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    )

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = 4.dp, y = (-2).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                tint = subtitleColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        OpenAiDropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            items = buildList {
                                add(
                                    OpenAiMenuItemData(
                                        title = "服务器设置",
                                        icon = LucideIcons.Bolt,
                                        onClick = onManage
                                    )
                                )
                                // 演示服务器没有真实网络，不提供端口映射
                                if (!server.isMock && server.authType != AuthType.DEMO_MOCK) {
                                    add(
                                        OpenAiMenuItemData(
                                            title = if (activeForwardCount > 0) "端口映射 ($activeForwardCount)" else "端口映射",
                                            icon = Icons.Rounded.SwapHoriz,
                                            iconTint = if (activeForwardCount > 0) AccentGreen else Color.Unspecified,
                                            onClick = onOpenPortForward
                                        )
                                    )
                                }
                                add(
                                    OpenAiMenuItemData(
                                        title = "删除服务器",
                                        icon = LucideIcons.Trash2,
                                        isDestructive = true,
                                        onClick = onDelete
                                    )
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 紧接着标题下方的描述文本
                val descText = server.description.ifBlank { "远程连接主机" }
                Text(
                    text = descText,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
