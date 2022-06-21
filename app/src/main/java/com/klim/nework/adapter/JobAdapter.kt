package com.klim.nework.adapter



import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.klim.nework.R
import com.klim.nework.databinding.JobListBinding
import com.klim.nework.dto.Job
import com.klim.nework.utils.AndroidUtils
import me.saket.bettermovementmethod.BetterLinkMovementMethod


interface OnJobButtonInteractionListener {
    fun onLinkClicked(url: String)
}

class JobAdapter(private val onJobButtonInteractionListener: OnJobButtonInteractionListener) :
    ListAdapter<Job, JobViewHolder>(JobDiffItemCallback) {
    object JobDiffItemCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = JobListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onJobButtonInteractionListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class JobViewHolder(
    private val binding: JobListBinding,
    private val onJobButtonInteractionListener: OnJobButtonInteractionListener
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
                    onJobButtonInteractionListener.onLinkClicked(url)
                    true
                }
        }

    }
}


