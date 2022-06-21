package com.klim.nework.adapter


import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.klim.nework.R
import com.klim.nework.databinding.JobMyListBinding
import com.klim.nework.dto.Job
import com.klim.nework.utils.AndroidUtils
import me.saket.bettermovementmethod.BetterLinkMovementMethod


interface OnJobMyButtonInteractionListener {
    fun onDeleteJob(job: Job)
    fun onLinkClicked(url: String)
    fun onEditJob(job: Job)
}

class JobMyAdapter(private val onJobMyButtonInteractionListener: OnJobMyButtonInteractionListener) :
    ListAdapter<Job, MyJobViewHolder>(JobDiffItemCallback) {
    object JobDiffItemCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyJobViewHolder {
        val binding = JobMyListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyJobViewHolder(binding, onJobMyButtonInteractionListener)
    }

    override fun onBindViewHolder(holder: MyJobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class MyJobViewHolder(
    private val binding: JobMyListBinding,
    private val onJobMyButtonInteractionListener: OnJobMyButtonInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job) {
        val startJob = AndroidUtils.formatMillisToDateString(job.start)
        val endJob = AndroidUtils.formatMillisToDateString(job.finish)
            ?: itemView.context.getString(R.string.job_blank_end_date_text)
        with(binding) {


            tvJobCompany.text = job.name
            tvJobPosition.text = job.position
            tvJobPeriod.text =
                itemView.context.getString(R.string.job_employment_term, startJob, endJob)
            tvJobLink.text = job.link

            BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, tvJobLink)

                .setOnLinkClickListener { textView, url ->
                    onJobMyButtonInteractionListener.onLinkClicked(url)
                    true
                }


            buttonJobOptions.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.list_menu)
                    //  menu.setGroupVisible(R.id.list_item_modification, post.ownedByMe)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_delete -> {
                                onJobMyButtonInteractionListener.onDeleteJob(job)
                                true
                            }
                            R.id.action_edit -> {
                                onJobMyButtonInteractionListener.onEditJob(job)
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