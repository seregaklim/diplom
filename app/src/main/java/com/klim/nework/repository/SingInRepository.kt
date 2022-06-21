package com.klim.nework.repository

import com.klim.nework.Api.ApiService
import com.klim.nework.dto.MediaUpload
import com.klim.nework.error.ApiError
import com.klim.nework.error.NetworkError
import com.klim.nework.error.UndefinedError
import com.klim.nework.model.AuthModel
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException
import javax.inject.Inject

class SignInUpRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun onSignIn(login: String, password: String): AuthModel {
        try {
            val response = apiService.signIn(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun onSignUp(login: String, password: String, userName: String): AuthModel {
        try {
            val response = apiService.signUp(login, password, userName)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UndefinedError
        }
    }

    suspend fun onSignUpWithAttachment(
        login: String,
        password: String,
        userName: String,
        mediaUpload: MediaUpload
    ): AuthModel {
        try {

            val loginPart = MultipartBody.Part.createFormData("login", login)
            val passPart = MultipartBody.Part.createFormData("pass", password)
            val namePart = MultipartBody.Part.createFormData("name", userName)
            val avatarPart = MultipartBody.Part.createFormData(
                "file", mediaUpload.file.name,
                mediaUpload.file.asRequestBody()
            )

            val response = apiService.signUpWithAvatar(loginPart, passPart, namePart, avatarPart)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }
            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            e.printStackTrace()
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UndefinedError
        }


    }
}
