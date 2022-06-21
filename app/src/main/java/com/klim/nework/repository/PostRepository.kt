package com.klim.nework.repository
import androidx.paging.PagingData
import com.klim.nework.dto.*
import kotlinx.coroutines.flow.Flow



interface PostRepository {
    val data: Flow<PagingData<Post>>

    suspend fun createPost(post: Post)
    suspend fun saveWithAttachment(post: Post, mediaUpload: MediaUpload, type : AttachmentType)
    suspend fun uploadMedia(mediaUpload: MediaUpload): MediaDownload
   suspend fun likePost(post: Post)
   suspend fun deletePost(postId: Long)

}