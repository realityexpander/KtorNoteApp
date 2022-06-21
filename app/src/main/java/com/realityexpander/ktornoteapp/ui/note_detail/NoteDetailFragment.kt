package com.realityexpander.ktornoteapp.ui.note_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.databinding.FragmentNoteDetailBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment

class NoteDetailFragment: BaseFragment(R.layout.fragment_note_detail) {

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}