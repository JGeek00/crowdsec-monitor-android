package com.jgeek00.crowdsecmonitor.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jgeek00.crowdsecmonitor.data.models.CSServer

@Database(entities = [CSServer::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun csServerDao(): CSServerDao

    companion object {
        const val DATABASE_NAME = "crowdsec_monitor_db"
    }
}
