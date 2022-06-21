package com.klim.nework.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.nework.auth.AppAuth
import com.klim.nework.dto.MediaUpload
import com.klim.nework.error.AppError
import com.klim.nework.model.FeedModel
import com.klim.nework.model.MediaModel
import com.klim.nework.repository.SignInUpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class LoginViewModel  @Inject constructor(
    private val repository: SignInUpRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    private val noPhoto = MediaModel()

    private val _isSignedIn = MutableLiveData(false)
    val isSignedIn: LiveData<Boolean>
        get() = _isSignedIn

    private val _dataState = MutableLiveData(FeedModel())
    val dataState: LiveData<FeedModel>
        get() = _dataState

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<MediaModel>
        get() = _photo

    fun invalidateSignedInState() {
        _isSignedIn.value = false
    }

    fun invalidateDataState() {
        _dataState.value = FeedModel()
    }

    fun onSignIn(login: String, password: String) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModel(isLoading = true)

                val idAndToken = repository.onSignIn(login, password)
                val id = idAndToken.userId ?: 0L
                val token = idAndToken.token ?: "N/A"
                appAuth.setAuth(id = id, token = token)

                _dataState.value = FeedModel(isLoading = false)
                _isSignedIn.value = true
            } catch (e: Exception) {
                _dataState.value = (FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

    fun onSignUp(login: String, password: String, userName: String) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModel(isLoading = true)

                when (_photo.value) {
                    noPhoto ->   repository.onSignUp(login, password, userName)
                    else -> _photo.value?.file?.let { file ->
                        repository.onSignUpWithAttachment(login, password, userName, MediaUpload(file))
                    }
                }

                _dataState.value = FeedModel(isLoading = false)
            } catch (e: Exception) {
                _dataState.value = (FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }


    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = MediaModel(uri, file)
    }
}