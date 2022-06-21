package com.klim.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.klim.nework.Converter.InstantDateConverter
import com.klim.nework.Converter.LongSetDataConverter

import com.klim.nework.Entity.*
import com.klim.nework.dao.*


@Database(
    entities = [PostEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
        PostRemoteKeyEntity::class,
        WallRemoteKeyEntity::class,
        WallPostEntity::class,
        JobEntity::class,
        UserEntity::class], version = 3
)
@TypeConverters(InstantDateConverter::class, LongSetDataConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun wallRemoteKeyDao(): WallRemoteKeyDao
    abstract fun wallPostDao(): WallPostDao
    abstract fun jobDao(): JobDao
    abstract fun userDao(): UserDao
}