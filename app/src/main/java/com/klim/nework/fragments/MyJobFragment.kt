package com.klim.nework.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.klim.nework.R
import com.klim.nework.adapter.*
import com.klim.nework.databinding.FragmentMyJobsBinding
import com.klim.nework.databinding.FragmentPageBinding
import com.klim.nework.dto.Job
import com.klim.nework.dto.Post
import com.klim.nework.fragments.EditJobDialogFragment.Companion.etCompany
import com.klim.nework.fragments.EditJobDialogFragment.Companion.etLink
import com.klim.nework.fragments.EditJobDialogFragment.Companion.etPosition
import com.klim.nework.fragments.EditJobDialogFragment.Companion.tvFinish
import com.klim.nework.fragments.EditJobDialogFragment.Companion.tvStart
import com.klim.nework.ui.MainActivity
import com.klim.nework.utils.loadCircleCrop
import com.klim.nework.viewModel.AuthViewModel
import com.klim.nework.viewModel.JobViewModel
import com.klim.nework.viewModel.PageViewModel
import com.klim.nework.viewModel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
@ExperimentalPagingApi
class MyJobFragment : Fragment() {

    val profileViewModel: PageViewModel by viewModels(
        ownerProducer = { this }
    )
    val jobViewModel: JobViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var navController: NavController
    private lateinit var postRecyclerView: PostRecyclerView

    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMyJobsBinding.inflate(inflater, container, false)
        navController = findNavController()

        val postViewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        val navArgs: PagerFragmentArgs by navArgs()

        profileViewModel.setAuthorId(navArgs.authorId)

        profileViewModel.getUserById()





        val jobMyAdapter = JobMyAdapter(object : OnJobMyButtonInteractionListener {

            override fun onDeleteJob(job: Job) {

                profileViewModel.deleteJobById(job.id)

            }


            override fun onEditJob (job: Job) {
//                profileViewModel.editJob(job)
                jobViewModel.editJob(job)
                findNavController().navigate(R.id.action_myJobFragment_to_editJobDialogFragment,


                    Bundle().apply {
                        etCompany = job.name
                        etLink = job.link
                        etPosition = job.position
                        tvFinish = job.finish.toString()
                        tvStart = job.start.toString()
                    })



            }


            override fun onLinkClicked(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }

        })


        binding.rvMyJob.adapter =jobMyAdapter



        profileViewModel.getAllJobs().observe(viewLifecycleOwner) {
            val oldCount = jobMyAdapter.itemCount
            jobMyAdapter.submitList(it) {
                if (it.size > oldCount) {
                    binding.rvMyJob.smoothScrollToPosition((0))
                }
            }
            binding.rvMyJob.isVisible = it.isNotEmpty()
        }
      //отключить
        fun onResumeActionBar() {
            super.onResume()
            (activity as AppCompatActivity).supportActionBar!!.hide()
        }
        onResumeActionBar()


        binding.rvMyJob.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )


        profileViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.hasError) {
                val msg = getString(state.errorMessage ?: R.string.common_error_message)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.ok_action), {})
                    .show()
                profileViewModel.invalidateDataState()
            }
        }

        return binding.root
    }
}
