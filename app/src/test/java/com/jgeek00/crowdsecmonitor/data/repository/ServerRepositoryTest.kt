package com.jgeek00.crowdsecmonitor.data.repository

import androidx.room.Room
import com.jgeek00.crowdsecmonitor.data.db.AppDatabase
import com.jgeek00.crowdsecmonitor.data.db.CSServerModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class ServerRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: ServerRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repo = ServerRepository(db.csServerDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getAllServers returns empty list initially`() = runBlocking {
        assertTrue(repo.getAllServers().first().isEmpty())
    }

    @Test
    fun `createServer inserts and sets first as default`() = runBlocking {
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
        val servers = repo.getAllServers().first()
        assertEquals(1, servers.size)
        assertEquals("My Server", servers[0].name)
        assertTrue(servers[0].defaultServer == true)
    }

    @Test
    fun `createServer with custom headers`() = runBlocking {
        repo.createServer(
            name = "With Headers",
            connectionMethod = "https",
            ipDomain = "example.com",
            port = null,
            path = "/api",
            authMethod = "bearer",
            basicUser = null,
            basicPassword = null,
            bearerToken = "tok123",
            customHeaders = listOf(Pair("X-Custom", "value1"))
        )
        val server = repo.getAllServers().first().first()
        assertEquals("With Headers", server.name)
        assertEquals("https", server.http)
        assertEquals("/api", server.path)
        assertEquals("bearerToken" to "tok123", "bearerToken" to (server.bearerToken ?: ""))
    }

    @Test
    fun `insert and retrieve by id`() = runBlocking {
        val id = UUID.randomUUID()
        val s = CSServerModel(id = id, name = "Test", http = "http", domain = "localhost",
            port = 8080, path = null, authMethod = "none",
            basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null)
        repo.insertServer(s)
        val found = repo.getServerById(id)
        assertNotNull(found)
        assertEquals(id, found!!.id)
    }

    @Test
    fun `delete server`() = runBlocking {
        val id = UUID.randomUUID()
        val s = CSServerModel(id = id, name = "ToDelete", http = "http", domain = "x",
            port = null, path = null, authMethod = "none",
            basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null)
        repo.insertServer(s)
        repo.deleteServer(s)
        assertNull(repo.getServerById(id))
    }

    @Test
    fun `setDefaultServer sets the correct server as default`() = runBlocking {
        repo.createServer(name = "First", connectionMethod = "http", ipDomain = "a",
            port = null, path = null, authMethod = "none",
            basicUser = null, basicPassword = null, bearerToken = null)
        repo.createServer(name = "Second", connectionMethod = "http", ipDomain = "b",
            port = null, path = null, authMethod = "none",
            basicUser = null, basicPassword = null, bearerToken = null)

        val servers = repo.getAllServers().first()
        val secondId = servers.find { it.name == "Second" }!!.id
        repo.setDefaultServer(secondId)

        val def = repo.getDefaultServer().first()
        assertEquals("Second", def!!.name)
    }
}
