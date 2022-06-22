package com.klim.nework.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.klim.nework.databinding.DialogFragmentMakeJobBinding
import com.klim.nework.utils.AndroidUtils
import com.klim.nework.utils.AndroidUtils.formatDateToDateString
import com.klim.nework.utils.StringArg
import com.klim.nework.viewModel.JobViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class EditJobDialogFragment: Fragment() {

    private val jobViewModel: JobViewModel by viewModels(
    ownerProducer = ::requireParentFragment,
    )



    lateinit var binding: DialogFragmentMakeJobBinding

    companion object {
        var Bundle.etCompany: String? by StringArg
        var Bundle.etPosition: String? by StringArg
        var Bundle.tvStart: String? by StringArg
        var Bundle.etLink: String? by StringArg
        var Bundle.tvFinish: String? by StringArg
    }

    @RequiresApi(Build.VERSION_CODES.O)
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


      jobViewModel.jobDateTime.observe(viewLifecycleOwner) { dateTime ->
            dateTime?.let {
                binding.tvStart.text = it
                binding.tvFinish.text = it
            }
        }


        val calendar = Calendar.getInstance()

        arguments?.etCompany?.let(binding.etCompany::setText)
        arguments?.etPosition?.let(binding.etPosition::setText)
         arguments?.tvStart?.let(binding.tvStart::setText)
         arguments?.tvFinish?.let(binding.tvFinish::setText)
        arguments?.etLink?.let(binding.etLink::setText)


        binding.tvStart.text= formatDateToDateString(calendar.time)
        binding.tvFinish.text= formatDateToDateString(calendar.time)


        binding.tvStart.setOnClickListener {


            val calendar = Calendar.getInstance()
            DateFragment(calendar) { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                binding.tvStart.text= formatDateToDateString(calendar.time)

            }.show(childFragmentManager, "datePicker")


        }


        binding.tvFinish.setOnClickListener {

            val calendar = Calendar.getInstance()
            DateFragment(calendar) { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                binding.tvFinish.text= formatDateToDateString(calendar.time)

            }.show(childFragmentManager, "datePicker")

        }

        binding.buttonCancel.setOnClickListener {

            findNavController().popBackStack()

        }

        binding.buttonProve.setOnClickListener {

            val  company = binding.etCompany.text.toString()
            val  position=  binding.etPosition.text.toString()
            val  link= binding.etLink.text.toString()

          val started = binding.tvStart.text
          val finished =binding.tvFinish.text

            jobViewModel.editedJob.value?.let {
                jobViewModel.saveJob(
                    it.copy(
                        name =  company,
                        position= position,
                        start =  AndroidUtils.formatDateStringToMillis(started as String),
                        finish =   AndroidUtils.formatDateStringToMillis(finished as String) ,
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