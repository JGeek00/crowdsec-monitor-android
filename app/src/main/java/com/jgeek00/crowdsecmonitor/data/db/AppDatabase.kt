package com.jgeek00.crowdsecmonitor.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CSServerModel::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun csServerDao(): CSServerDao

    companion object {
        const val DATABASE_NAME = "crowdsec_monitor_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE CSServers ADD COLUMN customHeaders TEXT")
            }
        }
    }
}
