package com.realityexpander.ktornoteapp.ui.add_edit_note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.databinding.FragmentAddEditNoteBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment

class AddEditNoteFragment: BaseFragment(R.layout.fragment_add_edit_note) {

    private var _binding: FragmentAddEditNoteBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditNoteBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}