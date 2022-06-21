package com.klim.nework.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.klim.nework.auth.AppAuth
import com.klim.nework.dto.*

import com.klim.nework.error.AppError
import com.klim.nework.model.FeedModel
import com.klim.nework.model.MediaModel
import com.klim.nework.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

@ExperimentalPagingApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
) : ViewModel() {

    private val cached: Flow<PagingData<Post>> = repository
        .data
//        .map { pagingData ->
//            pagingData.insertSeparators(
//                generator = { before, after ->
//                    if (before?.id?.rem(5) != 0L) null else
////                        Ad(
////                            Random.nextLong(),
////                            "https://netology.ru",
////                            "figma.jpg"
////                        )
//                }
//            )
//        }
        .cachedIn(viewModelScope)

    val data: Flow<PagingData<Post>> = appAuth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached
                .map { pagingData ->
                    pagingData.map { item ->
                        if (item !is Post) item else item.copy(ownedByMe = item.authorId == myId)
                    }
                }
        }

    private val noMedia = MediaModel()


    private val _dataState = MutableLiveData(FeedModel())
    val dataState: LiveData<FeedModel>
        get() = _dataState

    fun invalidateDataState() {
        _dataState.value = FeedModel()
    }

    private val _editedPost = MutableLiveData<Post?>(null)
    val editedPost: LiveData<Post?>
        get() = _editedPost

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media




    fun editPost(editedPost: Post) {
        _editedPost.value = editedPost
    }

    fun invalidateEditPost() {
        _editedPost.value = null
    }

    fun savePost(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedModel(isLoading = true))
                when (_media.value) {
                    noMedia -> repository.createPost(post)
                    else -> {
                        when (_media.value?.type) {
                            AttachmentType.IMAGE -> {
                                _media.value?.file?.let { file ->
                                    repository.saveWithAttachment(
                                        post,
                                        MediaUpload(file),
                                        AttachmentType.IMAGE
                                    )
                                }
                            }
                            AttachmentType.VIDEO -> {
                                _media.value?.file?.let { file ->
                                    repository.saveWithAttachment(
                                        post,
                                        MediaUpload(file),
                                        AttachmentType.VIDEO
                                    )
                                }
                            }
                            AttachmentType.AUDIO -> {
                                _media.value?.file?.let { file ->
                                    repository.saveWithAttachment(
                                        post,
                                        MediaUpload(file),
                                        AttachmentType.AUDIO
                                    )
                                }
                            }
                            null -> repository.createPost(post)
                        }
                    }


                }

                _dataState.value = FeedModel()
            } catch (e: Exception) {
                _dataState.value = (FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            } finally {
                invalidateEditPost()
                _media.value = noMedia
            }
        }
    }


    fun likePost(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModel()
                repository.likePost(post)
            } catch (e: Exception) {
                _dataState.value = (FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                _dataState.value = (FeedModel(isLoading = true))
                repository.deletePost(postId)
                _dataState.value = (FeedModel(isLoading = false))
            } catch (e: Exception) {
                _dataState.value = (FeedModel(
                    hasError = true,
                    errorMessage = AppError.getMessage(e)
                ))
            }
        }
    }

    fun changeMedia(uri: Uri?, file: File?, type: AttachmentType?) {
        _media.value = MediaModel(uri, file, type)
    }


}

