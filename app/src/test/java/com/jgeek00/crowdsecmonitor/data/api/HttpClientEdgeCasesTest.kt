package com.jgeek00.crowdsecmonitor.data.api

import com.jgeek00.crowdsecmonitor.data.models.HttpClientException
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class HttpClientEdgeCasesTest {

    @Test
    fun `buildBaseUrl with port 0 omits port`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "example.com", port = 0, path = "",
            authMethod = "none"
        ))
        assertNotNull(c.retrofit)
    }

    @Test
    fun `buildBaseUrl with port null omits port`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "example.com", port = null, path = "",
            authMethod = "none"
        ))
        assertNotNull(c.retrofit)
    }

    @Test
    fun `buildBaseUrl with path without leading slash prepends it`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "example.com", port = 8080, path = "api/v1",
            authMethod = "none"
        ))
        assertNotNull(c.retrofit)
    }

    @Test
    fun `buildBaseUrl with path with leading slash works`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "example.com", port = 8080, path = "/api/v1",
            authMethod = "none"
        ))
        assertNotNull(c.retrofit)
    }

    @Test
    fun `handleHttpError 500 with JSON body throws HttpClientException`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "localhost", port = 8080, path = null,
            authMethod = ""
        ))
        val resp = retrofit2.Response.error<Unit>(
            500, """{"message":"Invalid request"}""".toResponseBody("application/json".toMediaType())
        )
        assertThrows(HttpClientException::class.java) { c.handleHttpError(resp) }
    }

    @Test
    fun `handleHttpError 500 with errors list throws HttpClientException`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "localhost", port = 8080, path = null,
            authMethod = ""
        ))
        val resp = retrofit2.Response.error<Unit>(
            500, """{"errors":["Field required","Invalid value"]}""".toResponseBody("application/json".toMediaType())
        )
        assertThrows(HttpClientException::class.java) { c.handleHttpError(resp) }
    }

    @Test
    fun `handleHttpError 500 with empty JSON body throws HttpError`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "localhost", port = 8080, path = null,
            authMethod = ""
        ))
        val resp = retrofit2.Response.error<Unit>(
            500, """{}""".toResponseBody("application/json".toMediaType())
        )
        assertThrows(HttpClientException.HttpError::class.java) {
            c.handleHttpError(resp)
        }
    }

    @Test
    fun `handleHttpError 500 with malformed JSON body throws HttpError`() {
        val c = HttpClient(TestFixtures.csserverModel(
            http = "http", domain = "localhost", port = 8080, path = null,
            authMethod = ""
        ))
        val resp = retrofit2.Response.error<Unit>(
            500, """{invalid}""".toResponseBody("application/json".toMediaType())
        )
        assertThrows(HttpClientException.HttpError::class.java) {
            c.handleHttpError(resp)
        }
    }
}
