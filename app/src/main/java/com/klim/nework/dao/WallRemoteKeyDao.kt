package com.klim.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.klim.nework.Entity.WallRemoteKeyEntity


@Dao
interface WallRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM WallRemoteKeyEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT MIN(id) FROM WallRemoteKeyEntity")
    suspend fun getMinKey(): Long?

    @Query("SELECT MAX(id) FROM WallRemoteKeyEntity")
    suspend fun getMaxKey(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: WallRemoteKeyEntity)

    @Query("DELETE FROM WallRemoteKeyEntity")
    suspend fun removeAll()
}