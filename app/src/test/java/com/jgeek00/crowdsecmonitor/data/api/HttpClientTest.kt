package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Response
import retrofit2.http.GET

@RunWith(RobolectricTestRunner::class)
class HttpClientTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `buildBaseUrl constructs valid retrofit`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "example.com", port = 8080, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        ))
        assertNotNull(c.retrofit)
    }

    @Test
    fun `buildBaseUrl with https scheme`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "https", domain = "example.com", port = 443, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        ))
        assertNotNull(c.retrofit)
    }

    @Test
    fun `handleHttpError throws Unauthorized for 401`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "localhost", port = 8080, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        ))
        val resp = Response.error<Unit>(401, "".toResponseBody(null))
        assertThrows(HttpClientException.Unauthorized::class.java) { c.handleHttpError(resp) }
    }

    @Test
    fun `handleHttpError throws HttpError for 500`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "localhost", port = 8080, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        ))
        val resp = Response.error<Unit>(500, "".toResponseBody(null))
        assertThrows(HttpClientException.HttpError::class.java) { c.handleHttpError(resp) }
    }

    @Test
    fun `handleHttpError throws HttpError for 404 with no body`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "localhost", port = 8080, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        ))
        try {
            val resp = Response.error<Unit>(404, "".toResponseBody(null))
            c.handleHttpError(resp)
        } catch (e: HttpClientException.HttpError) {
            assertEquals(404, e.statusCode)
        }
    }

    interface ApiTestService {
        @GET("api/v1/test")
        suspend fun testGet(): Response<okhttp3.ResponseBody>
    }

    @Test
    fun `okHttpClient can make a request and receive response`() = kotlinx.coroutines.runBlocking {
        val server = TestFixtures.csserverModel(
            http = "http", domain = mockWebServer.hostName, port = mockWebServer.port, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        mockWebServer.enqueue(MockResponse().setBody("""{"status":"ok"}""").setResponseCode(200))
        val response = HttpClient(server).retrofit.create(ApiTestService::class.java).testGet()
        assertEquals(200, response.code())
        assertEquals("""{"status":"ok"}""", response.body()?.string())
    }
}
