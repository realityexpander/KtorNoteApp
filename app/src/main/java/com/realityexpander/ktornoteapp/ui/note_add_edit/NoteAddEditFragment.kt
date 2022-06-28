package com.realityexpander.ktornoteapp.ui.note_add_edit

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.common.Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.data.local.entities.millisToDateString
import com.realityexpander.ktornoteapp.databinding.FragmentNoteAddEditBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment
import com.realityexpander.ktornoteapp.ui.common.DEFAULT_NOTE_COLOR
import com.realityexpander.ktornoteapp.ui.common.NOTE_SHAPE_RESOURCE_ID
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

        setHasOptionsMenu(true) // show menu in toolbar

        // Was id passed for the Note we are editing?
        if (args.noteId.isNotEmpty()) {
            viewModel.getNoteById(args.noteId)
            subscribeToObservers()  // listen for the database result
        }

        _binding = FragmentNoteAddEditBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore ColorPickerDialogFragment state from config change
        if (savedInstanceState != null) {
            (parentFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                as? ColorPickerDialogFragment?)  // make this cast nullable to avoid crash
                ?.setupListeners()
        }

        // Setup click listener for swatch to open ColorPickerDialogFragment
        binding.viewNoteColor.setOnClickListener {
            ColorPickerDialogFragment(curNoteColor)
                .setupListeners()
                .show(parentFragmentManager, FRAGMENT_TAG) // FRAGMENT_TAG is to identify the dialog fragment upon config changes
        }

        // If creating a new Note, show default color in the swatch
        if (args.noteId.isEmpty()) showColorPickerSwatch()

    }

    private fun changeViewNoteColor(newColorString: String) {
        setDrawableColorTint(
            binding.viewNoteColor,
            NOTE_SHAPE_RESOURCE_ID,
            newColorString,
            resources
        )
        curNoteColor = newColorString
    }

    private fun ColorPickerDialogFragment.setupListeners(): ColorPickerDialogFragment {
        setPositiveListener { colorStr ->
            curNoteColor = colorStr
            changeViewNoteColor(colorStr)
        }

        return this
    }

    override fun onPause() {
        super.onPause()

        saveNote()
    }

    private fun subscribeToObservers() {
        viewModel.note.observe(viewLifecycleOwner) { eventResource ->
            if (!eventResource.hasBeenHandled) {
                eventResource.getContentOnlyOnce()?.let outer@{ resourceNote ->

                    when (resourceNote.status) {
                        Status.SUCCESS -> {
                            resourceNote.data?.let { note ->
                                curNote = note
                                curNoteColor = note.color
                                binding.etNoteTitle.setText(note.title)
                                binding.etNoteContent.setText(note.content)
                                showColorPickerSwatch()

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

    private fun showColorPickerSwatch() {
        binding.viewNoteColor.visibility = View.VISIBLE
        setDrawableColorTint(
            binding.viewNoteColor,
            NOTE_SHAPE_RESOURCE_ID,
            curNoteColor,
            resources
        )
    }

    private fun saveNote(): Boolean {
        val authUserId = sharedPref.getString(
            ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_USER_ID, "Unknown user"
        ) ?: "Unknown user"

        val title = binding.etNoteTitle.text.toString()
        val content = binding.etNoteContent.text.toString()
        if(title.isEmpty()) {
            showSnackbar("Please enter a title")
            return false
        }

        // Save the current date/time
        val dateMillis = System.currentTimeMillis()

        // Recreate note with a new note using most of the values as the old note
        val note = NoteEntity(
            id = curNote?.id ?: UUID.randomUUID().toString(),
            title = title,
            content = content,
            color = curNoteColor,
            owners = if(curNote?.owners.isNullOrEmpty()) listOf(authUserId)
                else curNote?.owners ?: listOf(authUserId),
            date = millisToDateString(dateMillis),
            dateMillis = dateMillis,
            createdAt = if(curNote?.createdAt == 0L) dateMillis
                else curNote?.createdAt ?: dateMillis,
            updatedAt = if (curNote?.createdAt != 0L) dateMillis else 0L,
        )

        viewModel.upsertNote(note)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_note_add_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miSave -> {
                findNavController().navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}