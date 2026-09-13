package com.bettershell.app

import com.bettershell.app.portforward.PortForwardTunnel
import com.bettershell.app.portforward.PortForwardValidation
import com.bettershell.app.portforward.TunnelStatus
import com.jcraft.jsch.JSchException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.BindException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class PortForwardValidationTest {

    // ==================== 远程端口 ====================

    @Test
    fun parseRemotePortAcceptsValidRange() {
        assertEquals(1, PortForwardValidation.parseRemotePort("1").getOrThrow())
        assertEquals(8000, PortForwardValidation.parseRemotePort("8000").getOrThrow())
        assertEquals(65535, PortForwardValidation.parseRemotePort("65535").getOrThrow())
        assertEquals(22, PortForwardValidation.parseRemotePort("  22  ").getOrThrow())
    }

    @Test
    fun parseRemotePortRejectsInvalidInput() {
        assertTrue(PortForwardValidation.parseRemotePort("").isFailure)
        assertTrue(PortForwardValidation.parseRemotePort("abc").isFailure)
        assertTrue(PortForwardValidation.parseRemotePort("0").isFailure)
        assertTrue(PortForwardValidation.parseRemotePort("65536").isFailure)
        assertTrue(PortForwardValidation.parseRemotePort("-1").isFailure)
    }

    // ==================== 本地端口 ====================

    @Test
    fun parseLocalPortTreatsBlankAsAutoAssign() {
        assertNull(PortForwardValidation.parseLocalPort("").getOrThrow())
        assertNull(PortForwardValidation.parseLocalPort("   ").getOrThrow())
    }

    @Test
    fun parseLocalPortEnforcesNonPrivilegedRange() {
        assertEquals(1024, PortForwardValidation.parseLocalPort("1024").getOrThrow())
        assertEquals(65535, PortForwardValidation.parseLocalPort("65535").getOrThrow())

        // 非 root 应用无法绑定 1024 以下的端口
        assertTrue(PortForwardValidation.parseLocalPort("1023").isFailure)
        assertTrue(PortForwardValidation.parseLocalPort("80").isFailure)
        assertTrue(PortForwardValidation.parseLocalPort("0").isFailure)
        assertTrue(PortForwardValidation.parseLocalPort("65536").isFailure)
        assertTrue(PortForwardValidation.parseLocalPort("abc").isFailure)
    }

    // ==================== 冲突检测 ====================

    private fun tunnel(
        serverId: String = "server-a",
        serverName: String = "A 服务器",
        remotePort: Int = 8000,
        localPort: Int = 8080,
        status: TunnelStatus = TunnelStatus.ACTIVE
    ) = PortForwardTunnel(
        id = "$serverId-$remotePort-$localPort",
        serverId = serverId,
        serverName = serverName,
        remotePort = remotePort,
        requestedLocalPort = localPort,
        localPort = localPort,
        status = status
    )

    @Test
    fun detectsDuplicateRemotePortOnSameServer() {
        val existing = listOf(tunnel())
        val conflict = PortForwardValidation.findConflict(existing, "server-a", 8000, null)
        assertNotNull("同一服务器的同一远程端口应视为冲突", conflict)
        assertTrue(conflict!!.contains("8080"))
    }

    @Test
    fun detectsLocalPortTakenByAnotherTunnel() {
        val existing = listOf(tunnel())
        val conflict = PortForwardValidation.findConflict(existing, "server-a", 9000, 8080)
        assertNotNull("本地端口已被占用应视为冲突", conflict)
        assertTrue(conflict!!.contains("8080"))
    }

    @Test
    fun detectsLocalPortTakenOnAnotherServer() {
        val existing = listOf(tunnel(serverId = "server-a", serverName = "A 服务器"))
        val conflict = PortForwardValidation.findConflict(existing, "server-b", 9000, 8080)
        assertNotNull("不同服务器之间本地端口同样不能重复", conflict)
        assertTrue(conflict!!.contains("A 服务器"))
    }

    @Test
    fun allowsIndependentPorts() {
        val existing = listOf(tunnel())
        assertNull(PortForwardValidation.findConflict(existing, "server-a", 9000, 9090))
        // 自动分配（null）不会与任何已有本地端口冲突
        assertNull(PortForwardValidation.findConflict(existing, "server-a", 9001, null))
    }

    @Test
    fun ignoresFinishedTunnels() {
        val failed = listOf(tunnel(status = TunnelStatus.ERROR))
        val stopped = listOf(tunnel(status = TunnelStatus.STOPPED))
        assertNull(PortForwardValidation.findConflict(failed, "server-a", 8000, 8080))
        assertNull(PortForwardValidation.findConflict(stopped, "server-a", 8000, 8080))
    }

    // ==================== 错误文案 ====================

    @Test
    fun describesAddressInUse() {
        val message = PortForwardValidation.describeError(BindException("Address already in use"))
        assertTrue(message.contains("本地端口已被占用"))
    }

    @Test
    fun describesAuthFailure() {
        val message = PortForwardValidation.describeError(JSchException("Auth fail"))
        assertTrue(message.contains("认证失败"))
    }

    @Test
    fun describesTimeout() {
        val message = PortForwardValidation.describeError(SocketTimeoutException("connect timed out"))
        assertTrue(message.contains("连接超时"))
    }

    @Test
    fun describesUnknownHost() {
        val message = PortForwardValidation.describeError(UnknownHostException("example.invalid"))
        assertTrue(message.contains("无法解析主机地址"))
    }

    @Test
    fun describesForwardingRejection() {
        val message = PortForwardValidation.describeError(JSchException("Port forwarding failed"))
        assertTrue(message.contains("拒绝端口转发"))
    }

    @Test
    fun fallsBackToGenericMessage() {
        val message = PortForwardValidation.describeError(RuntimeException())
        assertEquals("端口映射启动失败", message)
    }
}
