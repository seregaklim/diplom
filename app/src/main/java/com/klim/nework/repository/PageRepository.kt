package com.klim.nework.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.klim.nework.Api.ApiService
import com.klim.nework.Entity.*
import com.klim.nework.dao.JobDao
import com.klim.nework.dao.PostDao
import com.klim.nework.dao.WallPostDao
import com.klim.nework.dao.WallRemoteKeyDao
import com.klim.nework.db.AppDb
import com.klim.nework.dto.Job
import com.klim.nework.dto.Post
import com.klim.nework.dto.User
import com.klim.nework.error.ApiError
import com.klim.nework.error.DbError
import com.klim.nework.error.NetworkError
import com.klim.nework.error.UndefinedError
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject

class PageRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDb: AppDb,
    private val wallPostDao: WallPostDao,
    private val wallRemoteKeyDao: WallRemoteKeyDao,
    private val jobDao: JobDao,
    private val postDao: PostDao,
) {

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalPagingApi
    fun getAllPosts(authorId: Long): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = DEFAULT_WALL_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = WallRemoteMediator(
            apiService,
            appDb,
            wallPostDao,
            wallRemoteKeyDao,
            authorId
        ),
        pagingSourceFactory = { wallPostDao.getWallPagingSource() }
    ).flow.map { postList ->
        postList.map { it.toDto() }
    }

    fun getAllJobs(): LiveData<List<Job>> = jobDao.getAllJobs().map { jobList ->
        jobList.map {
            it.toDto()
        }
    }


    suspend fun getUserById(userId: Long): User {
        try {
            val response = apiService.getUserById(userId)
            if (!response.isSuccessful) throw ApiError(response.code())
            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun getLatestWallPosts(authorId: Long) {
        try {
            wallPostDao.clearPostTable()
            val response = apiService.getLatestWallPosts(authorId, 10)

            if (!response.isSuccessful) throw ApiError(response.code())

            val body = response.body() ?: throw ApiError(response.code())

            wallPostDao.insertPosts(body.toWallPostEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }


    suspend fun likePost(post: Post) {
        try {

            val likedPost = post.copy(
                likeCount = if (post.likedByMe) post.likeCount.dec()
                else post.likeCount.inc(),
                likedByMe = !post.likedByMe
            )
            wallPostDao.insertPost(WallPostEntity.fromDto(likedPost))


            val response = if (post.likedByMe) apiService.dislikeWallPostById(post.id)
            else apiService.likeWallPostById(post.id)

            if (!response.isSuccessful)
                throw ApiError(response.code())
        } catch (e: IOException) {

            wallPostDao.insertPost(WallPostEntity.fromDto(post))
            throw NetworkError
        } catch (e: SQLException) {
            wallPostDao.insertPost(WallPostEntity.fromDto(post))
            throw  DbError
        } catch (e: Exception) {
            wallPostDao.insertPost(WallPostEntity.fromDto(post))
            throw UndefinedError
        }
    }
/////
    suspend fun loadJobsFromServer(authorId: Long) {
        try {
            jobDao.removeAllJobs()
            val response = apiService.getAllUserJobs(authorId)

            if (!response.isSuccessful) throw ApiError(response.code())

            val body = response.body() ?: throw ApiError(response.code())

            jobDao.insertJobs(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun createJob(job: Job) {
        try {
            val response = apiService.saveJob(job)

            if (!response.isSuccessful) throw ApiError(response.code())

            val body = response.body() ?: throw ApiError(response.code())

            jobDao.insertJob(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun deleteJobById(id: Long) {
        val jobToDelete = jobDao.getJobById(id)
        try {
            jobDao.removeJobById(id)

            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                jobDao.insertJob(jobToDelete)
                throw ApiError(response.code())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun deletePost(postId: Long) {
        val postToDelete = postDao.getPostById(postId)
        try {
            postDao.deletePost(postId)
            wallPostDao.deletePost(postId)
            val response = apiService.deletePost(postId)
            if (!response.isSuccessful) {
                postDao.insertPost(postToDelete)
                wallPostDao.insertPost(WallPostEntity.fromDto(postToDelete.toDto()))
                throw ApiError(response.code())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: SQLException) {
            throw  DbError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }
}
