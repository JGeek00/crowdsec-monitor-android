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

    @TypeConverter
    fun headerListToString(headers: List<Pair<String, String>>?): String? {
        if (headers.isNullOrEmpty()) return null
        return headers.joinToString("\u001E") { (k, v) -> "$k\u001F$v" }
    }

    @TypeConverter
    fun stringToHeaderList(value: String?): List<Pair<String, String>>? {
        if (value.isNullOrEmpty()) return null
        return value.split("\u001E").mapNotNull { entry ->
            val parts = entry.split("\u001F", limit = 2)
            if (parts.size == 2) Pair(parts[0], parts[1]) else null
        }
    }
}
