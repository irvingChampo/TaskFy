package com.example.myapplication.src.Core.Room.Converters

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

class DateConverters {

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? {
        return value?.toEpochSecond(ZoneOffset.UTC)
    }

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
        }
    }
}