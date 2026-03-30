package com.jgeek00.crowdsecmonitor.data.repository

import com.jgeek00.crowdsecmonitor.data.db.CSServerDao
import com.jgeek00.crowdsecmonitor.data.models.CSServer
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(
    private val csServerDao: CSServerDao
) {
    fun getAllServers(): Flow<List<CSServer>> = csServerDao.getAllServers()

    fun getDefaultServer(): Flow<CSServer?> = csServerDao.getDefaultServer()

    suspend fun getServerById(id: UUID): CSServer? = csServerDao.getServerById(id)

    suspend fun insertServer(server: CSServer) = csServerDao.insertServer(server)

    suspend fun updateServer(server: CSServer) = csServerDao.updateServer(server)

    suspend fun deleteServer(server: CSServer) = csServerDao.deleteServer(server)

    suspend fun setDefaultServer(id: UUID) = csServerDao.setDefaultServer(id)

    suspend fun createServer(
        name: String,
        connectionMethod: String,
        ipDomain: String,
        port: Int?,
        path: String?,
        authMethod: String,
        basicUser: String?,
        basicPassword: String?,
        bearerToken: String?
    ) {
        val count = csServerDao.countServers()
        val isFirst = count == 0
        val server = CSServer(
            name = name,
            http = connectionMethod,
            domain = ipDomain,
            port = port,
            path = path,
            authMethod = authMethod,
            basicUser = basicUser,
            basicPassword = basicPassword,
            bearerToken = bearerToken,
            defaultServer = isFirst
        )
        csServerDao.insertServer(server)
    }
}
