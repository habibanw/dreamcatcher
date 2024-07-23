package edu.vt.cs5254.dreamcatcher.database

import androidx.room.TypeConverter
import java.util.Date

class DreamTypeConverters {

    @TypeConverter
    fun convertLongToDate(millis: Long) = Date(millis)

    @TypeConverter
    fun convertDateToLong(date: Date) = date.time

}