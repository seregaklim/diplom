package com.klim.nework.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.klim.nework.R
import com.klim.nework.adapter.LoadStateAdapter
import com.klim.nework.adapter.OnPostButtonInteractionListener
import com.klim.nework.adapter.PostAdapter
import com.klim.nework.adapter.PostRecyclerView
import com.klim.nework.databinding.FragmentsPostsBinding
import com.klim.nework.dto.Post
import com.klim.nework.viewModel.AuthViewModel
import com.klim.nework.viewModel.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest



@AndroidEntryPoint
class PostFragment: Fragment() {

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var navController: NavController
    private lateinit var recyclerView: PostRecyclerView


    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentsPostsBinding.inflate(inflater, container, false)
        val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
        )

        navController = findNavController()



        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(LoginFragments.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry) { success ->
                if (!success) {
                    val startDestination = navController.graph.startDestination
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    navController.navigate(startDestination, null, navOptions)
                }
            }

      recyclerView = binding.rvPosts

        val adapter = PostAdapter(object : OnPostButtonInteractionListener {

            override fun onPostLike(post: Post) {
                if (!authViewModel.isAuthenticated) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.error_unauthorized_to_like),
                        Snackbar.LENGTH_SHORT
                    )
                        .setAction(getString(R.string.ok_action), {})
                        .show()
                    return
                }
                viewModel.likePost(post)
            }

            override fun onPostRemove(post: Post) {
                viewModel.deletePost(post.id)
            }

            override fun onSharePost(post: Post) {

                if (!authViewModel.isAuthenticated) {
                    Snackbar.make(
                        binding.root,
                        "${getString(R.string.error_unauthorized_to_like)}",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .show()
                } else {

                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)

                }
            }


            override fun onPostEdit(post: Post) {
                viewModel.editPost(post)
                navController.navigate(R.id.action_nav_posts_fragment_to_makeEditPostFragment)
            }

            override fun onAvatarClicked(post: Post) {
                val action = PostFragmentDirections
                    .actionNavPostsFragmentToNavPageFragment(authorId = post.authorId)
                navController.navigate(action)
            }

            override fun onLinkClicked(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }

            override fun onContainerPostUser(post: Post){

                val action = PostFragmentDirections
                    .actionNavPostsFragmentToUserPostFragment(authorId = post.authorId)
                navController.navigate(action)

            }




        })

        binding.rvPosts.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )






        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        binding.rvPosts.itemAnimator = itemAnimator

        binding.rvPosts.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadStateAdapter { adapter.retry() },
            footer = LoadStateAdapter { adapter.retry() }
        )


        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.rvPosts.smoothScrollToPosition(0)
                }
            }
        })

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeToRefresh.isRefreshing = state.refresh == LoadState.Loading

                if (state.source.refresh is LoadState.NotLoading &&
                    state.append.endOfPaginationReached &&
                    adapter.itemCount < 1
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


        binding.swipeToRefresh.setOnRefreshListener {
            adapter.refresh()
        }
        return binding.root


    }


    override fun onResume() {
        if(::recyclerView.isInitialized) recyclerView.createPlayer()
        super.onResume()
    }

    override fun onPause() {
        if(::recyclerView.isInitialized) recyclerView.releasePlayer()
        super.onPause()
    }


    override fun onStop() {
        if(::recyclerView.isInitialized) recyclerView.releasePlayer()
        super.onStop()
    }

}
