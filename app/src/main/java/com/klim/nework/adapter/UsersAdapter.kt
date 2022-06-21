package com.klim.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.klim.nework.R
import com.klim.nework.databinding.UsersListBinding
import com.klim.nework.dto.User
import com.klim.nework.utils.loadCircleCrop


interface OnUserInteractionListener {
    fun onUserClicked(user: User)
}

class UsersAdapter(private val onUserInteractionListener: OnUserInteractionListener) :
    ListAdapter<User, UsersViewHolder>(UserDiffItemCallback) {
    object UserDiffItemCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding =
            UsersListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersViewHolder(binding, onUserInteractionListener)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}

class UsersViewHolder(
    private val binding: UsersListBinding,
    private val onUserInteractionListener: OnUserInteractionListener
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {
        with(binding) {
            tvUserName.text = user.name
            tvUserLogin.text = user.login

            user.avatar?.let {
                ivAvatar.loadCircleCrop(it)
            } ?: ivAvatar.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.user_no_avatar
                )
            )

            tvYou.visibility = if (user.isItMe) View.VISIBLE
            else View.GONE
        }

        binding.userItemContainer.setOnClickListener {
            onUserInteractionListener.onUserClicked(user)
        }
    }

}
