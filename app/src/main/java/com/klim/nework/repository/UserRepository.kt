package com.klim.nework.repository

import com.klim.nework.Api.ApiService
import com.klim.nework.Entity.toEntity
import com.klim.nework.dao.UserDao
import com.klim.nework.dto.Event
import com.klim.nework.dto.User
import com.klim.nework.error.ApiError
import com.klim.nework.error.DbError
import com.klim.nework.error.NetworkError
import com.klim.nework.error.UndefinedError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject


class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
) {

    fun getUsers() = userDao.getAllUsers().map { userList ->
        userList.map { it.toDto() }
    }

    suspend fun loadUsers() {
        try {
            userDao.removeAllUsers()
            val response = apiService.getAllUsers()

            if (!response.isSuccessful) throw ApiError(response.code())

            val body = response.body() ?: throw ApiError(response.code())

            userDao.insertUsers(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun getParticipatedEvent(id: Long) : Event {
        try {
            val response = apiService.getEventById(id)

            if (!response.isSuccessful) throw ApiError(response.code())

            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun getEventParticipants(eventId: Long): Flow<List<User>> {
        val event = getParticipatedEvent(eventId)
        return userDao.getEventParticipants(event.exhibitorsIds).map { participantsList ->
            participantsList.map { it.toDto() }
        }
    }
}