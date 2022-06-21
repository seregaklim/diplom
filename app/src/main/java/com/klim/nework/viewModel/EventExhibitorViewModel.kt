package com.klim.nework.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.nework.auth.AppAuth
import com.klim.nework.error.AppError
import com.klim.nework.model.FeedModel
import com.klim.nework.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class EventExhibitorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: AppAuth
) :
    ViewModel() {

    private val _dataState = MutableLiveData(FeedModel())
    val dataState: LiveData<FeedModel>
        get() = _dataState

    suspend fun getExhibitor(eventId: Long) = auth.authStateFlow.flatMapLatest { (myId, _) ->
        userRepository.getEventParticipants(eventId).map { userList ->
            userList.map { it.copy(isItMe = it.id == myId) }
        }
    }


    init {
        loadAllUsers()
    }


    private fun loadAllUsers() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModel(isLoading = true)
                userRepository.loadUsers()
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