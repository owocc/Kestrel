package com.bettershell.app.data

import kotlinx.serialization.Serializable

@Serializable
enum class AuthType {
    PASSWORD,
    PRIVATE_KEY,
    DEMO_MOCK
}

@Serializable
data class ServerConfig(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String = "root",
    val authType: AuthType = AuthType.PASSWORD,
    val password: String = "",
    val privateKey: String = "",
    val passphrase: String = "",
    val description: String = "", // 服务器自定义描述/用途说明，展示在卡片上保护真实IP和用户隐私
    val startupScript: String = "", // 自定义sh代码
    val isMock: Boolean = false,
    val lastConnected: Long = 0L
)
