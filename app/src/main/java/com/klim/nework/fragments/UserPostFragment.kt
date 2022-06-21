package com.klim.nework.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
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
import androidx.paging.filter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.klim.nework.R
import com.klim.nework.adapter.*
import com.klim.nework.databinding.FragmentPageBinding
import com.klim.nework.databinding.FragmentUserPostBinding
import com.klim.nework.databinding.FragmentUsersBinding
import com.klim.nework.databinding.FragmentsPostsBinding
import com.klim.nework.dto.AttachmentType
import com.klim.nework.dto.Job
import com.klim.nework.dto.Post
import com.klim.nework.dto.User
import com.klim.nework.ui.MainActivity
import com.klim.nework.utils.loadCircleCrop
import com.klim.nework.viewModel.AuthViewModel
import com.klim.nework.viewModel.PageViewModel
import com.klim.nework.viewModel.PostViewModel
import com.klim.nework.viewModel.UsersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class UserPostFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var recyclerView: PostRecyclerView
    private lateinit var navController: NavController
    val profileViewModel: PageViewModel by viewModels(
        ownerProducer = { this }
    )

    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val profileViewModel: PageViewModel by viewModels(ownerProducer = { this })
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val navArgsPostUser: UserPostFragmentArgs by navArgs()

        profileViewModel.setAuthorId(navArgsPostUser.authorId)
        profileViewModel.getUserById()



        navController = findNavController()


        val binding = FragmentUserPostBinding.inflate(inflater, container, false)


        val userPostAdapter = UserPostAdapter(object : OnUserPostButtonInteractionListener {

            override fun onContainerPostUser(post: Post) {

                val action = UserPostFragmentDirections
                    .actionUserPostFragmentToNavPageFragment(authorId = post.authorId)
                navController.navigate(action)


            }

        })



        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }


        // фото по столбикам
        fun init() {
            binding.apply {
                binding.rvPosts.layoutManager = GridLayoutManager(  requireContext(), 3)
                binding.rvPosts.adapter = userPostAdapter
            }
        }
        init()


        fun onResumeActionBar() {
            super.onResume()
            (activity as AppCompatActivity).supportActionBar!!.hide()
        }
        onResumeActionBar()


        binding.rvPosts.adapter =userPostAdapter





        requireContext()
        lifecycleScope.launchWhenCreated {
            profileViewModel.getWallPosts().collectLatest(userPostAdapter::submitData)
        }


        binding.rvPosts.itemAnimator = itemAnimator

        binding.rvPosts.adapter = userPostAdapter.withLoadStateHeaderAndFooter(
            header = LoadStateAdapter { userPostAdapter.retry() },
            footer = LoadStateAdapter { userPostAdapter.retry() }
        )


        userPostAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.rvPosts.smoothScrollToPosition(0)
                }
            }
        })

        lifecycleScope.launchWhenCreated {
            userPostAdapter.loadStateFlow.collectLatest { state ->
                binding.swipeToRefresh.isRefreshing = state.refresh == LoadState.Loading

                if (state.source.refresh is LoadState.NotLoading &&
                    state.append.endOfPaginationReached &&
                    userPostAdapter.itemCount < 2
                ) {
                    binding.emptyListCase.visibility = View.VISIBLE
                } else {
                    binding.emptyListCase.visibility = View.GONE
                }
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state.isLoading


            if (state.hasError) {
                val msg = getString(state.errorMessage ?: R.string.common_error_message)
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.ok_action), {})
                    .show()
                viewModel.invalidateDataState()
            }
        }



        return binding.root


    }


    override fun onResume() {
        if (::recyclerView.isInitialized) recyclerView.createPlayer()
        super.onResume()
    }

    override fun onPause() {
        if (::recyclerView.isInitialized) recyclerView.releasePlayer()
        super.onPause()
    }


    override fun onStop() {
        if (::recyclerView.isInitialized) recyclerView.releasePlayer()
        super.onStop()
    }



}