package com.klim.nework.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.klim.nework.Api.ApiService
import com.klim.nework.Entity.EventEntity
import com.klim.nework.Entity.EventRemoteKeyEntity
import com.klim.nework.Entity.toEntity
import com.klim.nework.dao.EventDao
import com.klim.nework.dao.EventRemoteKeyDao
import com.klim.nework.db.AppDb
import com.klim.nework.dto.Event
import com.klim.nework.error.ApiError


const val DEFAULT_EVENT_PAGE_SIZE: Int = 10

@ExperimentalPagingApi
class EventRemoteMediator(
    private val appDb: AppDb,
    private val remoteKeyDao: EventRemoteKeyDao,
    private val apiService: ApiService,
    private val eventDao: EventDao
) : RemoteMediator<Int, EventEntity>() {


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getLatestEvents(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    val maxKey = remoteKeyDao.getMaxKey() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getEventsAfter(maxKey, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val minKey = remoteKeyDao.getMinKey() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    apiService.getEventsBefore(minKey, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) throw ApiError(response.code())

            val receivedBody = response.body() ?: throw ApiError(response.code())

            if (receivedBody.isEmpty()) return MediatorResult.Success(
                endOfPaginationReached = true
            )

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        remoteKeyDao.removeAll()
                        insertMinKey(receivedBody)
                        insertMaxKey(receivedBody)
                        eventDao.clearEventTable()
                    }
                    LoadType.PREPEND -> insertMaxKey(receivedBody)
                    LoadType.APPEND -> insertMinKey(receivedBody)
                }
                eventDao.insertEvents(receivedBody.map {
                    it.copy(
                        likeCount = it.likeOwnerIds.size,
                        exhibitorsCount = it.exhibitorsIds.size
                    )
                }
                    .toEntity())
            }
            return MediatorResult.Success(
                endOfPaginationReached =
                receivedBody.isEmpty()
            )
        } catch (e: Exception) {
            e.printStackTrace()

            return MediatorResult.Error(e)
        }
    }


    private suspend fun insertMaxKey(receivedBody: List<Event>) {
        remoteKeyDao.insertKey(
            EventRemoteKeyEntity(
                type = EventRemoteKeyEntity.KeyType.AFTER,
                id = receivedBody.first().id
            )
        )
    }

    private suspend fun insertMinKey(receivedBody: List<Event>) {
        remoteKeyDao.insertKey(
            EventRemoteKeyEntity(
                type = EventRemoteKeyEntity.KeyType.BEFORE,
                id = receivedBody.last().id,
            )
        )
    }
}