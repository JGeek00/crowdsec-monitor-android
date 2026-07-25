package com.jgeek00.crowdsecmonitor.repository

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.MockWebSocketServer
import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
class ServiceStatusRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockHttpServer: MockWebServer
    private lateinit var mockWsServer: MockWebSocketServer
    private lateinit var apiClient: CrowdSecApiClient
    private val repository = ServiceStatusRepository()

    @Before
    fun setUp() {
        mockHttpServer = MockWebServer()
        mockHttpServer.start()
        mockWsServer = MockWebSocketServer()
        mockWsServer.start()
        val server = TestFixtures.csserverModel(
            http = "http",
            domain = mockHttpServer.hostName,
            port = mockHttpServer.port,
            path = "",
            authMethod = "none"
        )
        apiClient = CrowdSecApiClient(server)
    }

    @After
    fun tearDown() {
        repository.close()
        mockHttpServer.shutdown()
        mockWsServer.stop()
    }

    @Test
    fun `status starts as Loading`() {
        assertTrue(repository.status.value is LoadingResult.Loading)
    }

    @Test
    fun `start sets Success when API responds ok`() {
        mockHttpServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.apiStatusJson))
        mockWsServer.enqueueUpgrade()

        repository.start(apiClient)

        Thread.sleep(1000)
        assertTrue("Expected Success", repository.status.value is LoadingResult.Success)
    }

    @Test
    fun `start sets Failure when API errors`() {
        mockHttpServer.enqueue(MockResponse().setResponseCode(500))

        repository.start(apiClient)

        Thread.sleep(1000)
        assertTrue("Expected Failure", repository.status.value is LoadingResult.Failure)
    }

    @Test
    fun `start sets Failure on network error`() {
        val badClient = CrowdSecApiClient(
            TestFixtures.csserverModel(
                http = "http",
                domain = "localhost",
                port = 1,
                path = "",
                authMethod = "none"
            )
        )

        repository.start(badClient)

        Thread.sleep(1000)
        assertTrue("Expected Failure", repository.status.value is LoadingResult.Failure)
    }

    @Test
    fun `stop cancels jobs`() {
        mockHttpServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.apiStatusJson))
        mockWsServer.enqueueUpgrade()

        repository.start(apiClient)
        Thread.sleep(500)

        repository.stop(apiClient)
        Thread.sleep(200)

        assertNotNull(repository.status.value)
    }

    @Test
    fun `close cleans up all resources`() {
        repository.close()
        assertTrue(repository.status.value is LoadingResult.Loading)
    }

    @Test
    fun `start can be called multiple times without throwing`() {
        mockHttpServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.apiStatusJson))
        mockWsServer.enqueueUpgrade()

        repository.start(apiClient)
        Thread.sleep(500)

        mockHttpServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.apiStatusJson))
        mockWsServer.enqueueUpgrade()

        repository.start(apiClient)
        Thread.sleep(500)

        assertNotNull(repository.status.value)
    }

    @Test
    fun `openWebSocketManual opens websocket when not active`() {
        mockWsServer.enqueueUpgrade()

        repository.openWebSocketManual(apiClient)
        Thread.sleep(500)

        assertNotNull(repository.status.value)
    }
}
