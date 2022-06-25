package com.realityexpander.ktornoteapp.ui.note_detail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.common.isInternetConnected
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.databinding.FragmentNoteDetailBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment
import com.realityexpander.ktornoteapp.ui.dialogs.AddOwnerEmailDialog
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon

const val ADD_OWNER_DIALOG_TAG = "AddOwnerEmailDialog"

@AndroidEntryPoint
class NoteDetailFragment : BaseFragment(R.layout.fragment_note_detail) {

    private val viewModel by viewModels<NoteDetailViewModel>()

    private val args: NoteDetailFragmentArgs by navArgs()

    private var curNote: NoteEntity? = null

    private var _binding: FragmentNoteDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true) // show menu in toolbar

        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // restore dialog fragment
        if (savedInstanceState != null) {
            (parentFragmentManager.findFragmentByTag(ADD_OWNER_DIALOG_TAG)
                    as? AddOwnerEmailDialog?) // important to add nullable to cast to avoid crash
                ?.setupListeners()
        }

        // Was id passed for the Note we are editing?
        if (args.noteId.isNotEmpty()) {
            subscribeToObservers(args.noteId)  // listen for the database result
        } else {
            showSnackbar("Error: Note not found")
        }

        binding.fabEditNote.setOnClickListener {
            findNavController()
                .navigate(
                    NoteDetailFragmentDirections.actionNoteDetailFragmentToAddEditNoteFragment(
                        args.noteId
                    )
                )
        }
    }

    private fun addOwnerEmailToCurNote(ownerEmail: String) {
        curNote?.let { note ->
            viewModel.addOwnerEmailToNoteId(ownerEmail, note.id)
        }
    }

    private fun showAddOwnerEmailDialog() {
        AddOwnerEmailDialog()
            .setupListeners()
            .show(parentFragmentManager, ADD_OWNER_DIALOG_TAG)
    }

    private fun AddOwnerEmailDialog.setupListeners(): AddOwnerEmailDialog {
        setPositiveListener { ownerEmail ->
            addOwnerEmailToCurNote(ownerEmail)
        }

        return this
    }

    private fun setMarkdownText(markdownText: String) {
        val markwon = Markwon.create(requireContext())
        val markdown = markwon.toMarkdown(markdownText)

        markwon.setParsedMarkdown(binding.tvNoteContent, markdown)
    }

    private fun subscribeToObservers(noteId: String) {
        viewModel.observeNoteId(noteId).observe(viewLifecycleOwner) { note ->
            note?.let {
                curNote = note

                binding.tvNoteTitle.text = curNote!!.title
                setMarkdownText(curNote!!.content + "\n\n" +
                        // Get the owner emails for this note if the internet is connected
                        if (isInternetConnected(requireContext())) {
                            "---\n### owners: _" +
                            curNote!!.owners.joinToString(separator = ", ") { ownerId ->
                                viewModel.getEmailForOwnerId(ownerId)
                            } + "_"
                        } else {
                            "No internet connection - can't get owner emails, try again later"
                        }
                )
            } ?: run {
                showSnackbar("Error: Note not found")
            }
        }

        viewModel.addOwnerToNoteStatus.observe(viewLifecycleOwner) { EventResource ->
            when (EventResource.peekContent().status) {
                Status.SUCCESS -> {
                    binding.addOwnerProgressBar.visibility = View.GONE

                    if (EventResource.peekContent().data != null) {
                        if (EventResource.peekContent().data!!.isSuccessful) {
                            showSnackbar(
                                EventResource.getContentOnlyOnce()?.data?.message ?: "An unknown error occurred"
                            )
                        } else {
                            showSnackbar(
                                EventResource.getContentOnlyOnce()?.data?.message ?: "An unknown error occurred"
                            )
                        }
                    } else {
                        showSnackbar("observeAddOwnerIdToNoteIdStatus: data == null")
                    }
                }

                Status.ERROR -> {
                    binding.addOwnerProgressBar.visibility = View.GONE

                    showSnackbar(EventResource.getContentOnlyOnce()?.message!!)
                }

                Status.LOADING -> {
                    binding.addOwnerProgressBar.visibility = View.VISIBLE
                }
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_note_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAddOwner -> {
                showAddOwnerEmailDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}