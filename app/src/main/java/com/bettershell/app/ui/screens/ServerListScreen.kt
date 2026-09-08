package com.bettershell.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bettershell.app.data.AppThemeMode
import com.bettershell.app.data.AuthType
import com.bettershell.app.data.ServerConfig
import com.bettershell.app.data.ServerRepository
import com.bettershell.app.data.TerminalPreferencesRepository
import com.bettershell.app.ui.theme.AccentCyan
import com.bettershell.app.ui.theme.AccentGreen
import com.bettershell.app.ui.theme.AccentOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    repository: ServerRepository,
    prefsRepository: TerminalPreferencesRepository,
    onSelectServer: (ServerConfig) -> Unit
) {
    val servers by repository.servers.collectAsState()
    val terminalPrefs by prefsRepository.preferences.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingServer by remember { mutableStateOf<ServerConfig?>(null) }
    val scope = rememberCoroutineScope()

    val isDark = when (terminalPrefs.themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Terminal,
                                contentDescription = "Terminal Logo",
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "BetterShell",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "AI Agent Shell & Mobile Terminal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val nextMode = if (isDark) AppThemeMode.LIGHT else AppThemeMode.DARK
                            prefsRepository.updateThemeMode(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "切换主题模式",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            editingServer = null
                            showAddEditDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Server",
                            tint = MaterialTheme.colorScheme.onSurface
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
                onClick = {
                    editingServer = null
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Rounded.Add, "Add Server") },
                text = { Text("添加服务器", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (servers.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无服务器配置",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "点击下方按钮添加远程 SSH 服务器或本地 Agent 节点",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "已配置的服务器 (${servers.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
                        )
                    }

                    items(servers, key = { it.id }) { server ->
                        ServerCard(
                            server = server,
                            onConnect = { onSelectServer(server) },
                            onEdit = {
                                editingServer = server
                                showAddEditDialog = true
                            },
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

    if (showAddEditDialog) {
        AddEditServerDialog(
            initialServer = editingServer,
            onDismiss = {
                showAddEditDialog = false
                editingServer = null
            },
            onSave = { updated ->
                scope.launch {
                    if (editingServer != null) {
                        repository.updateServer(updated)
                    } else {
                        repository.addServer(updated)
                    }
                }
                showAddEditDialog = false
                editingServer = null
            }
        )
    }
}

@Composable
fun ServerCard(
    server: ServerConfig,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable(onClick = onConnect),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (server.isMock) AccentGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (server.isMock) AccentGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (server.isMock) Icons.Rounded.SmartToy else Icons.Rounded.Terminal,
                            contentDescription = null,
                            tint = if (server.isMock) AccentGreen else AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = server.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            val badgeLabel = when (server.authType) {
                                AuthType.DEMO_MOCK -> "Demo"
                                AuthType.PASSWORD -> "密码"
                                AuthType.PRIVATE_KEY -> "密钥"
                            }
                            val badgeColor = when (server.authType) {
                                AuthType.DEMO_MOCK -> AccentGreen
                                AuthType.PASSWORD -> AccentOrange
                                AuthType.PRIVATE_KEY -> AccentCyan
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = badgeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "${server.username}@${server.host}:${server.port}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable(onClick = onConnect)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Connect",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("编辑配置", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("删除服务器", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            }

            if (server.startupScript.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "启动脚本: ${server.startupScript.lines().firstOrNull { it.isNotBlank() } ?: ""}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
