package com.klim.nework.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.nework.auth.AppAuth
import com.klim.nework.dto.Job
import com.klim.nework.error.AppError
import com.klim.nework.model.FeedModel
import com.klim.nework.repository.PageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject




@HiltViewModel
class  JobViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val repository: PageRepository
) : ViewModel() {


    private val _dataState = MutableLiveData(FeedModel())
    val dataState: LiveData<FeedModel>
        get() = _dataState

    fun invalidateDataState() {
        _dataState.value = FeedModel()
    }


    private val _editedJob = MutableLiveData<Job?>(null)
    val editedJob: LiveData<Job?>
        get() = _editedJob

    fun editJob(editedJob: Job) {
        _editedJob.value = editedJob
    }

    fun invalidateEditedJob() {
        _editedJob.value = null
    }


    private val _jobDateTime = MutableLiveData<String?>()
    val jobDateTime: LiveData<String?>
        get() = _jobDateTime


    fun invalidateJobDateTime() {
        _jobDateTime.value = null
    }

    fun setJobDateTime(dateTime: String) {
        _jobDateTime.value = dateTime
    }

    fun saveJob(job: Job) {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedModel(isLoading = true))

                repository.createJob(job)


                _dataState.value = (FeedModel(isLoading = false))
            } catch (e: Exception) {
                _dataState.value = (FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            } finally {
                invalidateEditedJob()
                invalidateJobDateTime()
            }
        }
    }


    fun deleteJobById(id: Long) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModel(isLoading = true)
                repository.deleteJobById(id)
                _dataState.value = FeedModel(isLoading = false)
            } catch (e: Exception) {
                _dataState.value = FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                )
            }
        }
    }


}