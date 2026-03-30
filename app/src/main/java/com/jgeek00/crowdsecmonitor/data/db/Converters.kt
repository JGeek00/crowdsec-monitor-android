package com.jgeek00.crowdsecmonitor.data.db

import androidx.room.TypeConverter
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromString(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun uuidToString(uuid: UUID?): String? {
        return uuid?.toString()
    }
}
