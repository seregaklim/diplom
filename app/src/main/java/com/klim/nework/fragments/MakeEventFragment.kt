package com.klim.nework.fragments

import android.app.Activity
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import com.klim.nework.R
import com.klim.nework.databinding.FragmentMakeEventBinding
import com.klim.nework.dto.Event
import com.klim.nework.dto.EventType
import com.klim.nework.ui.MainActivity
import com.klim.nework.utils.AndroidUtils
import com.klim.nework.utils.loadImage
import com.klim.nework.viewModel.EventViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.util.*

@ExperimentalPagingApi
@AndroidEntryPoint
class MakeEventFragment : Fragment() {

    private lateinit var binding: FragmentMakeEventBinding
    private val viewModel: EventViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private var eventType: EventType? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMakeEventBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)


        viewModel.editedEvent.observe(viewLifecycleOwner) { editedEvent ->
            editedEvent?.let {
                (activity as MainActivity?)
                    ?.setActionBarTitle(getString(R.string.change_event_fragment_title))

                binding.etPostContenta.setText(editedEvent.content)
                binding.etPostContenta.requestFocus(
                    binding.etPostContenta.text.lastIndex
                )

                binding.tvEventDateTime.text =
                    AndroidUtils.formatMillisToDateTimeString(editedEvent.datetime.toEpochMilli())
                AndroidUtils.showKeyboard(binding.etPostContenta)

                it.attachment?.let { attachment ->
                    val attachmentUri = attachment.url
                    viewModel.changePhoto(attachmentUri.toUri(), null)
                    binding.ivPhoto.loadImage(attachmentUri)
                    //disable media removal
                    binding.buttonRemovePhoto.visibility = View.GONE
                }


            }
        }

        binding.groupChooseEventDate.setOnClickListener {
            showDateTimePicker()
        }



        viewModel.eventDateTime.observe(viewLifecycleOwner) { dateTime ->
            dateTime?.let {
                binding.tvEventDateTime.text = it
            }
        }

        val handlePhotoResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val resultCode = activityResult.resultCode
                val data = activityResult.data

                if (resultCode == Activity.RESULT_OK) {
                    val fileUri = data?.data!!
                    viewModel.changePhoto(fileUri, fileUri.toFile())
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Snackbar.make(
                        binding.root,
                        ImagePicker.getError(activityResult.data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        binding.buttonPickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(arrayOf("image/png", "image/jpeg"))
                .createIntent { intent ->
                    handlePhotoResult.launch(intent)
                }
        }

        binding.buttonTakePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .provider(ImageProvider.CAMERA)
                .createIntent { intent ->
                    handlePhotoResult.launch(intent)
                }
        }


        binding.buttonRemovePhoto.setOnClickListener {
            viewModel.changePhoto(null, null)
        }


        viewModel.photo.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel.uri == null) {
                binding.layoutPhotoCase.visibility = View.GONE
                return@observe
            }

            binding.layoutPhotoCase.visibility = View.VISIBLE
            binding.ivPhoto.setImageURI(photoModel.uri)
        }

        return binding.root
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DateFragment(calendar) { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)


            TimeFragment(calendar) { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                viewModel.setEventDateTime(
                    AndroidUtils.formatDateToDateTimeString(calendar.time)
                )
            }.show(childFragmentManager, "timePicker")
        }.show(childFragmentManager, "datePicker")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_make_edite_menu, menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                if (binding.etPostContenta.text.isNullOrEmpty()) {
                    binding.etPostContenta.error = getString(R.string.empty_field_error)
                    return false
                }

                if (binding.tvEventDateTime.text.isNullOrEmpty()) {
                    binding.tvEventDateTime.error = getString(R.string.empty_field_error)
                    return false
                }


                val content = binding.etPostContenta.text.toString()
                val date =
                    AndroidUtils.formatDateTimeStringToMillis(binding.tvEventDateTime.text.toString())
                val eventType = eventType ?: EventType.OFFLINE



                viewModel.editedEvent.value?.let {
                    viewModel.changePhoto(null, null)
                    viewModel.saveEvent(
                        it.copy(
                            content = content,
                            datetime = Instant.ofEpochMilli(date),
                            type = eventType,
                        )
                    )
                } ?: viewModel.saveEvent(
                    Event(
                        content = content,
                        datetime = Instant.ofEpochMilli(date),
                        type = eventType,
                    )
                )
                AndroidUtils.hideKeyboard(requireView())
                findNavController().popBackStack()
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAlert()
                }
            })
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.discard_changes_dialog_title))
        builder.setMessage(getString(R.string.discard_changes_dialog_body))
        builder.setPositiveButton(
            getString(R.string.action_leave_dialog_fragment),
            DialogInterface.OnClickListener { dialog, which ->
                viewModel.invalidateEditedEvent()
                viewModel.invalidateEventDateTime()
                viewModel.changePhoto(null, null)
                findNavController().navigateUp()
            })
        builder.setNeutralButton(
            getString(R.string.action_cancel_dialog_fragment),
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
        val dialog = builder.create()
        dialog.show()
    }
}