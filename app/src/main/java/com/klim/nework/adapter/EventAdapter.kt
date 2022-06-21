package com.klim.nework.adapter

import android.os.Build
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.klim.nework.R
import com.klim.nework.databinding.EventListBinding
import com.klim.nework.dto.Event
import com.klim.nework.dto.Post
import com.klim.nework.utils.AndroidUtils
import com.klim.nework.utils.loadCircleCrop
import com.klim.nework.utils.loadImage
import me.saket.bettermovementmethod.BetterLinkMovementMethod

interface OnEventButtonInteractionListener {
    fun onEventEdit(event: Event)
    fun onEventRemove(event: Event)
    fun onAvatarClicked(event: Event)
    fun onLinkClicked(url: String)
    fun onShareEvent(event: Event)
    fun onEventLike(event: Event)
}

class EventAdapter(private val interactionListener: OnEventButtonInteractionListener) :
    PagingDataAdapter<Event, EventViewHolder>(EventDiffCallback) {

    companion object EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id


        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val eventBinding =
            EventListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return EventViewHolder(eventBinding, interactionListener)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

}


class EventViewHolder(
    private val eventBinding: EventListBinding,
    private val interactionListener: OnEventButtonInteractionListener
) :
    RecyclerView.ViewHolder(eventBinding.root) {


    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(event: Event) {
        with(eventBinding) {
            tvUserName.text = event.author
            tvPublished.text =
                AndroidUtils.formatMillisToDateTimeString(event.published.toEpochMilli())
            tvContents.text = event.content
            BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, tvContents)
                .setOnLinkClickListener { textView, url ->
                    interactionListener.onLinkClicked(url)
                    true
                }

            tvEventDueDate.text =
                AndroidUtils.formatMillisToDateTimeString(event.datetime.toEpochMilli())


            event.authorAvatar?.let {
                ivAvatar.loadCircleCrop(it)
            } ?: ivAvatar.setImageDrawable(
                AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.user_no_avatar
                )
            )
            buttonLike.isChecked = event.likedByMe
            buttonLike.text = event.likeCount.toString()

            event.attachment?.let {
                imageAttachments.loadImage(it.url)
            }
            if (event.attachment == null) {
                mediaCase.visibility = View.GONE
            } else {
                mediaCase.visibility = View.VISIBLE
            }


            ivAvatar.setOnClickListener {
                interactionListener.onAvatarClicked(event)
            }



            buttonShare.setOnClickListener {
                interactionListener.onShareEvent(event)
            }

            buttonLike.setOnClickListener {
                interactionListener.onEventLike(event)
            }


            if (!event.ownedByMe) {
                buttonEventsOptions.visibility = View.GONE
            } else {
                buttonEventsOptions.visibility = View.VISIBLE
                buttonEventsOptions.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.list_menu)
                        menu.setGroupVisible(R.id.list_item_modification, event.ownedByMe)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.action_delete -> {
                                    interactionListener.onEventRemove(event)
                                    true
                                }
                                R.id.action_edit -> {
                                    interactionListener.onEventEdit(event)
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