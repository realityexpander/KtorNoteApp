package com.realityexpander.ktornoteapp.ui.note_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.databinding.FragmentNoteDetailBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon

@AndroidEntryPoint
class NoteDetailFragment: BaseFragment(R.layout.fragment_note_detail) {

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

        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Was id passed for the Note we are editing?
        if (args.noteId.isNotEmpty()) {
            subscribeToObservers(args.noteId)  // listen for the database result
        } else {
            showSnackbar("Error: Note not found")
        }

        binding.fabEditNote.setOnClickListener {
            findNavController()
                .navigate(NoteDetailFragmentDirections.actionNoteDetailFragmentToAddEditNoteFragment(args.noteId))
        }
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
                setMarkdownText(curNote!!.content)
            } ?: run {
                showSnackbar("Error: Note not found")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}