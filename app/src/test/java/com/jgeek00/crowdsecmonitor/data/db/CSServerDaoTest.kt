package com.jgeek00.crowdsecmonitor.data.db

import androidx.room.Room
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
class CSServerDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CSServerDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.csServerDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun server(
        id: UUID = UUID.randomUUID(),
        name: String = "Test",
        http: String = "http",
        domain: String = "localhost",
        port: Int? = 8080,
        path: String? = null,
        authMethod: String = "none",
        defaultServer: Boolean? = false
    ) = CSServerModel(
        id = id, name = name, http = http, domain = domain,
        port = port, path = path, authMethod = authMethod,
        basicUser = null, basicPassword = null, bearerToken = null,
        defaultServer = defaultServer, customHeaders = null
    )

    @Test
    fun `insert and retrieve server`() = runBlocking {
        val s = server()
        dao.insertServer(s)
        val all = dao.getAllServers().first()
        assertEquals(1, all.size)
        assertEquals(s.id, all[0].id)
    }

    @Test
    fun `getServerById returns null for missing`() = runBlocking {
        assertNull(dao.getServerById(UUID.randomUUID()))
    }

    @Test
    fun `getServerById returns server`() = runBlocking {
        val s = server()
        dao.insertServer(s)
        val found = dao.getServerById(s.id)
        assertNotNull(found)
        assertEquals(s.name, found!!.name)
    }

    @Test
    fun `countServers returns 0 for empty`() = runBlocking {
        assertEquals(0, dao.countServers())
    }

    @Test
    fun `countServers returns count`() = runBlocking {
        dao.insertServer(server())
        dao.insertServer(server())
        assertEquals(2, dao.countServers())
    }

    @Test
    fun `updateServer updates fields`() = runBlocking {
        val s = server(name = "Original")
        dao.insertServer(s)
        dao.updateServer(s.copy(name = "Updated"))
        val found = dao.getServerById(s.id)
        assertEquals("Updated", found!!.name)
    }

    @Test
    fun `deleteServer removes server`() = runBlocking {
        val s = server()
        dao.insertServer(s)
        dao.deleteServer(s)
        assertNull(dao.getServerById(s.id))
    }

    @Test
    fun `setDefaultServer makes only one default`() = runBlocking {
        val s1 = server(defaultServer = true)
        val s2 = server()
        dao.insertServer(s1)
        dao.insertServer(s2)
        dao.setDefaultServer(s2.id)
        val default = dao.getDefaultServer().first()
        assertEquals(s2.id, default!!.id)
    }

    @Test
    fun `getDefaultServer returns null when none is default`() = runBlocking {
        dao.insertServer(server(name = "First"))
        val default = dao.getDefaultServer().first()
        assertNull(default)
    }

    @Test
    fun `getAllServers returns all servers`() = runBlocking {
        dao.insertServer(server(name = "A"))
        dao.insertServer(server(name = "B"))
        dao.insertServer(server(name = "C"))
        val all = dao.getAllServers().first()
        assertEquals(3, all.size)
    }
}
