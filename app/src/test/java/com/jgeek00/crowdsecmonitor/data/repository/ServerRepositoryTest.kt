package com.jgeek00.crowdsecmonitor.data.repository

import com.jgeek00.crowdsecmonitor.data.db.CSServerDao
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class ServerRepositoryTest {

    private val dao = mockk<CSServerDao>(relaxed = true)
    private val repo = ServerRepository(dao)

    @Test
    fun `getAllServers returns list from DAO`() = runBlocking {
        val servers = listOf(
            CSServerModel(id = UUID.randomUUID(), name = "A", http = "http", domain = "a", port = null, path = null, authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null)
        )
        every { dao.getAllServers() } returns MutableStateFlow(servers)

        val result = repo.getAllServers().first()

        assertEquals(servers, result)
        coVerify { dao.getAllServers() }
    }

    @Test
    fun `getDefaultServer returns flow from DAO`() = runBlocking {
        val server = CSServerModel(id = UUID.randomUUID(), name = "Default", http = "http", domain = "d", port = null, path = null, authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null)
        every { dao.getDefaultServer() } returns MutableStateFlow(server)

        val result = repo.getDefaultServer().first()

        assertEquals(server, result)
        coVerify { dao.getDefaultServer() }
    }

    @Test
    fun `insertServer calls DAO insert`() = runBlocking {
        val server = CSServerModel(id = UUID.randomUUID(), name = "Test", http = "http", domain = "localhost", port = 8080, path = null, authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null)

        repo.insertServer(server)

        coVerify { dao.insertServer(server) }
    }

    @Test
    fun `updateServer calls DAO update`() = runBlocking {
        val server = CSServerModel(id = UUID.randomUUID(), name = "Test", http = "http", domain = "localhost", port = 8080, path = null, authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null)

        repo.updateServer(server)

        coVerify { dao.updateServer(server) }
    }

    @Test
    fun `deleteServer calls DAO delete`() = runBlocking {
        val server = CSServerModel(id = UUID.randomUUID(), name = "Test", http = "http", domain = "localhost", port = 8080, path = null, authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null)

        repo.deleteServer(server)

        coVerify { dao.deleteServer(server) }
    }

    @Test
    fun `getServerById calls DAO`() = runBlocking {
        val id = UUID.randomUUID()
        val server = CSServerModel(id = id, name = "Test", http = "http", domain = "localhost", port = 8080, path = null, authMethod = "none", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null)
        coEvery { dao.getServerById(id) } returns server

        val result = repo.getServerById(id)

        assertEquals(server, result)
        coVerify { dao.getServerById(id) }
    }

    @Test
    fun `setDefaultServer calls DAO setDefaultServer`() = runBlocking {
        val id = UUID.randomUUID()

        repo.setDefaultServer(id)

        coVerify { dao.setDefaultServer(id) }
    }

    @Test
    fun `createServer with no existing servers sets defaultServer true`() = runBlocking {
        coEvery { dao.countServers() } returns 0
        val serverSlot = slot<CSServerModel>()

        repo.createServer(
            name = "My Server",
            connectionMethod = "http",
            ipDomain = "192.168.1.1",
            port = 8080,
            path = null,
            authMethod = "none",
            basicUser = null,
            basicPassword = null,
            bearerToken = null
        )

        coVerify { dao.insertServer(capture(serverSlot)) }
        assertTrue(serverSlot.captured.defaultServer == true)
    }

    @Test
    fun `createServer with existing servers sets defaultServer false`() = runBlocking {
        coEvery { dao.countServers() } returns 1
        val serverSlot = slot<CSServerModel>()

        repo.createServer(
            name = "Another",
            connectionMethod = "https",
            ipDomain = "example.com",
            port = null,
            path = "/api",
            authMethod = "bearer",
            basicUser = null,
            basicPassword = null,
            bearerToken = "tok123",
            customHeaders = listOf(Pair("X-Custom", "v1"))
        )

        coVerify { dao.insertServer(capture(serverSlot)) }
        assertTrue(serverSlot.captured.defaultServer == false)
        assertEquals("bearerToken" to "tok123", "bearerToken" to serverSlot.captured.bearerToken!!)
    }
}
