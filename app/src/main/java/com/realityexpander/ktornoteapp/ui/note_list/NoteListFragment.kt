package com.realityexpander.ktornoteapp.ui.note_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.databinding.FragmentAuthBinding
import com.realityexpander.ktornoteapp.databinding.FragmentNoteListBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment

class NoteListFragment: BaseFragment(R.layout.fragment_note_list) {

    private var _binding: FragmentNoteListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(NoteListFragmentDirections.actionNotesListFragmentToAddEditNoteFragment(""))
        }

        showSnackbar("Hello from NoteListFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}