package com.klim.nework.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.klim.nework.R
import com.klim.nework.adapter.OnUserInteractionListener
import com.klim.nework.adapter.UsersAdapter
import com.klim.nework.databinding.FragmentEventsExhibitorBinding

import com.klim.nework.dto.User
import com.klim.nework.viewModel.EventExhibitorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import com.klim.nework.fragments.EventExhibitorFragmentArgs
import com.klim.nework.fragments.EventExhibitorFragmentDirections



@AndroidEntryPoint
class EventExhibitorFragment : Fragment() {

    lateinit var binding: FragmentEventsExhibitorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentEventsExhibitorBinding.inflate(inflater, container, false)
        val viewModel: EventExhibitorViewModel by viewModels()
        val navArgs: EventExhibitorFragmentArgs by navArgs()

        val eventId = navArgs.eventId

        val adapter = UsersAdapter(object : OnUserInteractionListener {
            override fun onUserClicked(user: User) {
                val userId = user.id
                val action =
                    EventExhibitorFragmentDirections.actionEventExhibitorFragmentToNavPageFragment(
                        userId
                    )
                findNavController().navigate(action)
            }
        })

        binding.rvExhibitor.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.getExhibitor(eventId).collectLatest {
                adapter.submitList(it)
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
}