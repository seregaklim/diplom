package com.klim.nework.adapter

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.material.snackbar.Snackbar
import com.klim.nework.Api.ApiServiceModule.Companion.BASE_URL
import com.klim.nework.databinding.PostListBinding
import com.klim.nework.dto.AttachmentType
import com.klim.nework.dto.Post
import com.klim.nework.utils.AndroidUtils
import com.klim.nework.utils.loadCircleCrop
import com.klim.nework.utils.loadImage
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import com.klim.nework.BuildConfig
import com.klim.nework.fragments.binding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContentProviderCompat.requireContext
import com.klim.nework.Api.ApiServiceModule.Companion.BASE_URL
import com.klim.nework.R

import com.klim.nework.fragments.binding


interface OnPostButtonInteractionListener {
    fun onPostRemove(post: Post)
    fun onPostEdit(post: Post)
    fun onAvatarClicked(post: Post)
    fun onLinkClicked(url: String)
    fun onSharePost(post: Post)
    fun onPostLike(post: Post)
    fun onContainerPostUser(post: Post)
}

class PostAdapter(
    private val interactionListener: OnPostButtonInteractionListener,
) :
    PagingDataAdapter<Post, PostViewHolder>(PostDiffCallback) {

    companion object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val postBinding =
            PostListBinding.inflate(LayoutInflater.from(parent.context), parent, false
            )
        return PostViewHolder(postBinding, interactionListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }


}


class PostViewHolder(
    private val postBinding: PostListBinding,
    private val interactionListener: OnPostButtonInteractionListener,

    ) :
    RecyclerView.ViewHolder(postBinding.root) {

    private val parentView = postBinding.root
    val videoThumbnail = postBinding.videosketch
    val videoContainer = postBinding.videoCase
    val videoProgressBar = postBinding.videoProgressbar
    var videoPreview: MediaItem? = null
    val videoPlayIcon: ImageView = postBinding.ivVideoPlayIcon



    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(post: Post) {
        parentView.tag = this

        with(postBinding) {

            tvUserName.text = post.author
            tvPublished.text = AndroidUtils.formatMillisToDateTimeString(post.published.toEpochMilli())
            tvContent.text = post.content
            BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, tvContent)
                .setOnLinkClickListener { textView, url ->
                    interactionListener.onLinkClicked(url)
                    true
                }


            post.authorAvatar?.let {
                ivAvatar.loadCircleCrop(it)
            } ?: ivAvatar.setImageDrawable(
                AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.ic_no_avatar_user
                )
            )

            ivAvatar.setOnClickListener {
                interactionListener.onAvatarClicked(post)
            }

            buttonLike.isChecked = post.likedByMe
            buttonLike.text = post.likeCount.toString()

            buttonLike.setOnClickListener {
                interactionListener.onPostLike(post)
            }
            buttonShare.isChecked
            buttonShare.text

            buttonShare.setOnClickListener {
                interactionListener. onSharePost(post)}



            imageAttachment.setOnClickListener{

                interactionListener.onContainerPostUser(post)
            }


            if (post.attachment == null) {
                imageAttachment.visibility = View.GONE

                videoCase.visibility = View.GONE

                videoPreview = null

            } else {
                when (post.attachment!!.type) {
                    AttachmentType.IMAGE -> {
                        videoPreview = null
                        videoCase.visibility = View.GONE
                        imageAttachment.visibility = View.VISIBLE
                        imageAttachment.loadImage(post.attachment!!.url)
                    }
                    AttachmentType.VIDEO -> {
                        imageAttachment.visibility = View.GONE
                        videoCase.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(post.attachment!!.url)
                        Glide.with(parentView).load(post.attachment!!.url).into(videoThumbnail)
                    }
                    AttachmentType.AUDIO -> {
                        imageAttachment.visibility = View.GONE
                        videoCase.visibility = View.VISIBLE
                        videoPreview = MediaItem.fromUri(post.attachment!!.url)
                        videoThumbnail.setImageDrawable(
                            AppCompatResources.getDrawable(
                                itemView.context,
                                R.drawable.ic_audiotrack_24
                            )
                        )
                    }
                }
            }

            if (!post.ownedByMe) {
                buttonPostOptions.visibility = View.GONE
            } else {
                buttonPostOptions.visibility = View.VISIBLE
                buttonPostOptions.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.list_menu)
                        menu.setGroupVisible(R.id.list_item_modification, post.ownedByMe)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_delete -> {
                                    interactionListener.onPostRemove(post)
                                    true
                                }
                                R.id.action_edit -> {
                                    interactionListener.onPostEdit(post)
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }
            }


        }
    }
}

data class PostPayload(
    val liked: Boolean? = null
)

