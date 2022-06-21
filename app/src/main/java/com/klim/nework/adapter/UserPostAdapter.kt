package com.klim.nework.adapter

import android.os.Build
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.klim.nework.R
import com.klim.nework.databinding.PostListBinding
import com.klim.nework.databinding.UserPostListBinding

import com.klim.nework.dto.AttachmentType
import com.klim.nework.dto.Post
import com.klim.nework.fragments.binding
import com.klim.nework.utils.AndroidUtils
import com.klim.nework.utils.loadCircleCrop
import com.klim.nework.utils.loadImage
import me.saket.bettermovementmethod.BetterLinkMovementMethod

interface OnUserPostButtonInteractionListener {

    fun onContainerPostUser(post: Post)
}

class UserPostAdapter(

    private val interactionListener: OnUserPostButtonInteractionListener,
) :
    PagingDataAdapter<Post, UserPostViewHolder>(UserPostDiffCallback) {

    companion object UserPostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPostViewHolder {
        val userPostBinding =
            UserPostListBinding.inflate(LayoutInflater.from(parent.context), parent, false
            )
        return UserPostViewHolder(userPostBinding, interactionListener)
    }

    override fun onBindViewHolder(holder: UserPostViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

}

class UserPostViewHolder(
    private val userPostBinding: UserPostListBinding,
    private val interactionListener: OnUserPostButtonInteractionListener,

    ) :
    RecyclerView.ViewHolder(userPostBinding.root) {


    fun bind(post: Post) {


        with(userPostBinding) {





            imageAttachment.setOnClickListener {
                interactionListener.onContainerPostUser(post)
            }


            if (post.attachment == null) {
                imageAttachment.visibility = View.GONE

                videoCase.visibility = View.GONE

            } else {
                videoCase.visibility = View.GONE
                imageAttachment.visibility = View.VISIBLE
                imageAttachment.loadImage(post.attachment!!.url)
            }


        }
    }
}



