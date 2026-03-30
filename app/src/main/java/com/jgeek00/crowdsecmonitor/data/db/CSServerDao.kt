package com.jgeek00.crowdsecmonitor.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CSServerDao {
    @Query("SELECT * FROM CSServers")
    fun getAllServers(): Flow<List<CSServerModel>>

    @Query("SELECT * FROM CSServers WHERE id = :id")
    suspend fun getServerById(id: UUID): CSServerModel?

    @Query("SELECT * FROM CSServers WHERE defaultServer = 1 LIMIT 1")
    fun getDefaultServer(): Flow<CSServerModel?>

    @Query("SELECT COUNT(*) FROM CSServers")
    suspend fun countServers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: CSServerModel)

    @Update
    suspend fun updateServer(server: CSServerModel)

    @Delete
    suspend fun deleteServer(server: CSServerModel)

    @Query("UPDATE CSServers SET defaultServer = 0")
    suspend fun resetDefaultServers()

    @Transaction
    suspend fun setDefaultServer(id: UUID) {
        resetDefaultServers()
        val server = getServerById(id)
        server?.let {
            updateServer(it.copy(defaultServer = true))
        }
    }
}
