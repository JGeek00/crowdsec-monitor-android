package com.jgeek00.crowdsecmonitor.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkModelsTest {

    // ── LoadingResult ───────────────────────────────────────────

    @Test
    fun `Loading state reports isLoading true and no data or error`() {
        val loading = LoadingResult.Loading as LoadingResult<Any>
        assertTrue(loading.isLoading)
        assertNull(loading.data)
        assertNull(loading.error)
    }

    @Test
    fun `Success state reports isLoading false and has data`() {
        val success = LoadingResult.Success("test-value") as LoadingResult<Any>
        assertEquals(false, success.isLoading)
        assertEquals("test-value", success.data)
        assertNull(success.error)
    }

    @Test
    fun `Failure state reports isLoading false and has error`() {
        val throwable = RuntimeException("test-error")
        val failure = LoadingResult.Failure(throwable) as LoadingResult<Any>
        assertEquals(false, failure.isLoading)
        assertNull(failure.data)
        assertEquals(throwable, failure.error)
    }

    @Test
    fun `Loading is the same singleton across reads`() {
        val a = LoadingResult.Loading
        val b = LoadingResult.Loading
        assertTrue(a === b)
    }

    // ── ApiErrorResponse.resolvedMessage ────────────────────────

    @Test
    fun `resolvedMessage returns message when set`() {
        val error = ApiErrorResponse(message = "Something went wrong", errors = listOf("err1"))
        assertEquals("Something went wrong", error.resolvedMessage)
    }

    @Test
    fun `resolvedMessage returns first error when message is null`() {
        val error = ApiErrorResponse(message = null, errors = listOf("first error", "second error"))
        assertEquals("first error", error.resolvedMessage)
    }

    @Test
    fun `resolvedMessage returns null when message null and errors empty`() {
        val error = ApiErrorResponse(message = null, errors = emptyList())
        assertNull(error.resolvedMessage)
    }

    @Test
    fun `resolvedMessage returns null when both message and errors are null`() {
        val error = ApiErrorResponse(message = null, errors = null)
        assertNull(error.resolvedMessage)
    }

    @Test
    fun `resolvedMessage returns null when both message and errors are null via defaults`() {
        val error = ApiErrorResponse()
        assertNull(error.resolvedMessage)
    }
}
