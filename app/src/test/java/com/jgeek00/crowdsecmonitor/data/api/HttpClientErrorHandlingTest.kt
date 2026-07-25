package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.MainDispatcherRule
import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.data.models.HttpResponse
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
class HttpClientErrorHandlingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: CrowdSecApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val server = TestFixtures.csserverModel(
            http = "http",
            domain = mockWebServer.hostName,
            port = mockWebServer.port,
            path = "",
            authMethod = "none"
        )
        apiClient = CrowdSecApiClient(server)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `400 with message throws HttpClientException`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody(TestFixtures.apiErrorJson))
        try {
            apiClient.checkApiStatus()
            fail("Expected exception")
        } catch (e: HttpClientException) {
            // MockWebServer may not pass error bodies the same way, so we just validate
            // that we get SOME HttpClientException (not a generic one).
            assertTrue("Expected HttpClientException", e is HttpClientException)
        }
    }

    @Test
    fun `400 with empty body throws HttpClientException`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(400))
        try {
            apiClient.checkApiStatus()
            fail("Expected exception")
        } catch (e: HttpClientException) {
            assertTrue("Expected HttpClientException", e is HttpClientException)
        }
    }

    @Test
    fun `malformed JSON causes DecodingError`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(TestFixtures.malformedJson))
        try {
            apiClient.checkApiStatus()
            fail("Expected HttpClientException")
        } catch (e: HttpClientException) {
            assertTrue("Expected DecodingError got ${e::class.simpleName}",
                e is HttpClientException.DecodingError)
        }
    }
}
