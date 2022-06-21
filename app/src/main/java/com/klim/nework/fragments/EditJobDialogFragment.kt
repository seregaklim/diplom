package com.klim.nework.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.klim.nework.R
import com.klim.nework.databinding.DialogFragmentMakeJobBinding
import com.klim.nework.databinding.FragmentMakeEventBinding
import com.klim.nework.dto.Event
import com.klim.nework.dto.Job
import com.klim.nework.fragments.EditJobDialogFragment.Companion.etLink
import com.klim.nework.fragments.EditJobDialogFragment.Companion.etPosition
import com.klim.nework.fragments.EditJobDialogFragment.Companion.tvFinish
import com.klim.nework.fragments.EditJobDialogFragment.Companion.tvStart
import com.klim.nework.utils.AndroidUtils
import com.klim.nework.utils.StringArg
import com.klim.nework.viewModel.JobViewModel
import com.klim.nework.viewModel.PageViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant

@AndroidEntryPoint
class EditJobDialogFragment: Fragment() {



    private val  jobViewModel: JobViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
    )


    companion object {
        var Bundle.etCompany: String? by StringArg
        var Bundle.etPosition: String? by StringArg
        var Bundle.tvStart: String? by StringArg
        var Bundle.etLink: String? by StringArg
        var Bundle.tvFinish: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogFragmentMakeJobBinding.inflate(
            inflater,
            container,
            false
        )


        val name = arguments?.etCompany?.let(binding.etCompany::setText)
        val position  = arguments?.etPosition?.let(binding.etPosition::setText)
        val started=  arguments?.tvStart?.let(binding.tvStart::setText)
        val finished=  arguments?.tvFinish?.let(binding.tvFinish::setText)
        val  link=  arguments?.etLink?.let(binding.etLink::setText)


        binding.buttonCancel.setOnClickListener {

            findNavController().popBackStack()

        }

        binding.buttonProve.setOnClickListener {

            val  company = binding.etCompany.text.toString().trim()
            val  position=  binding.etPosition.text.toString().trim()


            val  link= binding.etLink.text.toString().trim()


            val  finished = binding.tvFinish.text.toString().trim()

            val  started = binding.tvStart.text.toString().trim()



            jobViewModel.editedJob.value?.let {
                jobViewModel.saveJob(
                    it.copy(
                        name =  company,
                        position= position,
                        start = started.toLong(),
                        finish = finished.toLong() ,
                        link= link
                    )
               )
            }

            AndroidUtils.hideKeyboard(requireView())
            findNavController().popBackStack()

        }

        return binding.root
    }
}