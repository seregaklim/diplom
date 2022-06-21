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
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.klim.nework.R
import com.klim.nework.adapter.EventAdapter
import com.klim.nework.adapter.LoadStateAdapter
import com.klim.nework.adapter.OnEventButtonInteractionListener
import com.klim.nework.databinding.FragmentsEventsBinding
import com.klim.nework.dto.Event
import com.klim.nework.viewModel.AuthViewModel
import com.klim.nework.viewModel.EventViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class EventsFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private lateinit var binding: FragmentsEventsBinding

    @ExperimentalPagingApi
    private val viewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private lateinit var navController: NavController

    @ExperimentalPagingApi
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentsEventsBinding.inflate(inflater, container, false)
        navController = findNavController()

        val adapter = EventAdapter(object : OnEventButtonInteractionListener {


            override fun onShareEvent(event: Event) {
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
                        putExtra(Intent.EXTRA_TEXT, event.content)
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)

                }

            }

            override fun onEventLike(event: Event) {
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
                viewModel.likeEvent(event)
            }

            override fun onEventEdit(event: Event) {
                viewModel.editEvent(event)
                navController.navigate(R.id.action_nav_events_fragment_to_makeEventFragment)
            }

            override fun onEventRemove(event: Event) {
                viewModel.deleteEvent(event.id)
            }

            override fun onAvatarClicked(event: Event) {
                val action = EventsFragmentDirections
                    .actionNavEventsFragmentToNavPageFragment(authorId = event.authorId)
                navController.navigate(action)
            }

            override fun onLinkClicked(url: String) {
                CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                    .launchUrl(requireContext(), Uri.parse(url))
            }

        })

        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        binding.rvEvents.itemAnimator = itemAnimator

        binding.rvEvents.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoadStateAdapter { adapter.retry() },
            footer = LoadStateAdapter { adapter.retry() })
        binding.rvEvents.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        fun onResumeActionBar() {
            super.onResume()
            (activity as AppCompatActivity).supportActionBar!!.hide()
        }
        onResumeActionBar()
        
        lifecycleScope.launchWhenCreated {
            viewModel.eventList.collectLatest {
                adapter.submitData(it)
            }
        }
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (positionStart == 0) {
                    binding.rvEvents.smoothScrollToPosition(0)
                }
            }
        })


        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeToRefresh.isRefreshing = state.refresh is LoadState.Loading

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

}