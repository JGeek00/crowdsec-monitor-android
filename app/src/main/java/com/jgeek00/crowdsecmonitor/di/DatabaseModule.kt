package com.jgeek00.crowdsecmonitor.di

import android.content.Context
import androidx.room.Room
import com.jgeek00.crowdsecmonitor.data.db.AppDatabase
import com.jgeek00.crowdsecmonitor.data.db.CSServerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideCSServerDao(database: AppDatabase): CSServerDao {
        return database.csServerDao()
    }
}
