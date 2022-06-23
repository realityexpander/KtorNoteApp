package com.realityexpander.ktornoteapp.ui.note_add_edit

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.common.Constants.DEFAULT_NOTE_COLOR
import com.realityexpander.ktornoteapp.common.Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.data.local.entities.millisToDateString
import com.realityexpander.ktornoteapp.databinding.FragmentNoteAddEditBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment
import com.realityexpander.ktornoteapp.ui.common.setDrawableColorTint
import com.realityexpander.ktornoteapp.ui.dialogs.ColorPickerDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

const val FRAGMENT_TAG = "NoteAddEditFragment"

@AndroidEntryPoint
class NoteAddEditFragment : BaseFragment(R.layout.fragment_note_add_edit) {

    private val viewModel: NoteAddEditViewModel by viewModels()

    private val args: NoteAddEditFragmentArgs by navArgs()

    // Values for the Note we are currently editing
    private var curNote: NoteEntity? = null
    private var curNoteColor = DEFAULT_NOTE_COLOR

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var _binding: FragmentNoteAddEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (args.id.isNotEmpty()) {
            viewModel.getNoteById(args.id)
            subscribeToObservers()
        }

        _binding = FragmentNoteAddEditBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore ColorPickerDialogFragment state from config change
        if (savedInstanceState != null) {
            val colorPickerDialog = parentFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                as ColorPickerDialogFragment?  // make this cast nullable to avoid crash
            colorPickerDialog?.setPositiveListener { colorStr ->
                curNoteColor = colorStr
                changeViewNoteColor(colorStr)
            }
        }

        // Setup ColorPickerDialogFragment
        binding.viewNoteColor.setOnClickListener {
            ColorPickerDialogFragment().apply {
                setPositiveListener { colorStr ->
                    curNoteColor = colorStr
                    changeViewNoteColor(colorStr)
                }
            }.show(parentFragmentManager, FRAGMENT_TAG) // FRAGMENT_TAG is to identify the dialog fragment upon config changes
        }

        // TODO Add save button to toolbar
//        binding.saveButton.setOnClickListener {
//            saveNote()
//        }
    }

    private fun changeViewNoteColor(newColorString: String) {
        setDrawableColorTint(binding.viewNoteColor,
            R.drawable.circle_shape,
            newColorString,
            resources
        )
        curNoteColor = newColorString
    }

    override fun onPause() {
        super.onPause()
        saveNote()
    }

    private fun subscribeToObservers() {
        viewModel.note.observe(viewLifecycleOwner) { eventResource ->
            if (!eventResource.hasBeenHandled) {
                eventResource.getContentIfNotHandled()?.let outer@{ resourceNote ->

                    when (resourceNote.status) {
                        Status.SUCCESS -> {
                            resourceNote.data?.let { note ->
                                curNote = note
                                curNoteColor = note.color
                                binding.etNoteTitle.setText(note.title)
                                binding.etNoteContent.setText(note.content)
                                setDrawableColorTint(binding.viewNoteColor,
                                    R.drawable.circle_shape,
                                    note.color,
                                    resources
                                )

                                return@outer
                            }

                            showSnackbar(resourceNote.message ?: "An Error occurred")
                        }
                        Status.ERROR -> {
                            showSnackbar(resourceNote.message ?: "Note not found")
                        }
                        Status.LOADING -> {
                            // do nothing
                        }
                    }
                }
            }
        }
    }


    private fun saveNote() {
        val authUserId = sharedPref.getString(
            ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID, "Unknown user"
        ) ?: "Unknown user"

        val title = binding.etNoteTitle.text.toString()
        val content = binding.etNoteContent.text.toString()
        if(title.isEmpty() || content.isEmpty()) {
            showSnackbar("Please enter a title and color")
            return
        }

        // Update the date to the current time
        val dateMillis = System.currentTimeMillis()

        val note = NoteEntity(
            id = curNote?.id ?: UUID.randomUUID().toString(),
            title = title,
            content = content,
            color = curNoteColor,
            owners = if(curNote?.owners.isNullOrEmpty()) listOf(authUserId)
                else curNote?.owners ?: listOf(authUserId),
            date = millisToDateString(dateMillis),
            dateMillis = dateMillis
        )

        viewModel.upsertNote(note)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}