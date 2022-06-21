package com.klim.nework.viewModel

import androidx.lifecycle.*
import com.klim.nework.auth.AppAuth

import com.klim.nework.error.AppError
import com.klim.nework.model.FeedModel
import com.klim.nework.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UserRepository,
    auth: AppAuth
) : ViewModel() {

    private val _dataState = MutableLiveData(FeedModel())
    val dataState: LiveData<FeedModel>
        get() = _dataState

    init {
        loadAllUsers()
    }

    private val users = repository.getUsers()

    @ExperimentalCoroutinesApi
    val userList = auth.authStateFlow.flatMapLatest { (myId, _) ->
        users.map { list ->
            list.map { userItem ->
                userItem.copy(isItMe = myId == userItem.id)
            }
        }
    }.asLiveData(Dispatchers.Default)


    private fun loadAllUsers() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModel(isLoading = true)
                repository.loadUsers()
                _dataState.value = FeedModel()
            } catch (e: Exception) {
                _dataState.value = FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }

    fun refreshUsers() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModel(isRefreshing = true)
                repository.loadUsers()
                _dataState.value = FeedModel()
            } catch (e: Exception) {
                _dataState.value = FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }

    fun invalidateDataState() {
        _dataState.value = FeedModel()
    }
}