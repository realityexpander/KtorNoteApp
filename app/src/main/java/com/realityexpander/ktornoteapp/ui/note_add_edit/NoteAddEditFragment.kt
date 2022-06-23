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
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

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

    private fun subscribeToObservers() {
        viewModel.note.observe(viewLifecycleOwner) { eventResource ->
            if (!eventResource.hasBeenHandled) {
                eventResource.getContentIfNotHandled()?.let outer@{ resourceNote ->

                    when (resourceNote.status) {
                        Status.SUCCESS -> {
                            resourceNote.data?.let { note ->
                                curNote = note
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
                            showSnackbar(resourceNote.message ?: "An Error occurred")
                        }
                        Status.LOADING -> {
                            // showLoading()
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