package com.jgeek00.crowdsecmonitor.repository

import com.jgeek00.crowdsecmonitor.data.api.CrowdSecApiClient
import com.jgeek00.crowdsecmonitor.data.models.LoadingResult
import com.jgeek00.crowdsecmonitor.fixtures.TestFixtures
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class ServiceStatusRepositoryTest {

    private var apiClient: CrowdSecApiClient? = null
    private val repository = ServiceStatusRepository()

    @Before
    fun setUp() {
        val server = TestFixtures.csserverModel(
            http = "http", domain = "localhost", port = 8080, path = null,
            authMethod = "", basicUser = null, basicPassword = null, bearerToken = null, customHeaders = null
        )
        apiClient = CrowdSecApiClient(server)
    }

    @After
    fun tearDown() {
        repository.close()
    }

    @Test
    fun `status starts as Loading`() {
        assertTrue(repository.status.value is LoadingResult.Loading)
    }
}
