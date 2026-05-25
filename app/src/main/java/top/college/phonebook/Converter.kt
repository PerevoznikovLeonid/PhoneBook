package top.college.phonebook

import androidx.room.TypeConverter
import java.util.Date

class Converter {
    @TypeConverter
    fun fromDateToTimestamp(date: Date): Long = date.time

    @TypeConverter
    fun fromTimestampToDate(timestamp: Long): Date = Date(timestamp)
}