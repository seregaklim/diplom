package com.klim.nework.Converter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant


class LongSetDataConverter {
    @TypeConverter
    fun fromLongSet(set: Set<Long>): String {
        val gson = Gson()
        val objectType = object : TypeToken<Set<Long>>() {}.type
        return gson.toJson(set, objectType)
    }


    @TypeConverter
    fun toLongSet(value: String): Set<Long> {
        val gson = Gson()
        val objectType = object : TypeToken<Set<Long>>() {}.type
        return gson.fromJson(value, objectType)
    }
}
class InstantDateConverter {

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromInstantToMillis(instant: Instant): Long =
        instant.toEpochMilli()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromMillisToInstant(milis: Long): Instant =
        Instant.ofEpochMilli(milis)
}

