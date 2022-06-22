package com.realityexpander.ktornoteapp.ui.note_list

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.data.remote.BasicAuthInterceptor
import com.realityexpander.ktornoteapp.databinding.FragmentNoteListBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment
import com.realityexpander.ktornoteapp.ui.common.removeAllCredentials
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NoteListFragment: BaseFragment(R.layout.fragment_note_list) {

    private val viewModel: NoteListViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private var _binding: FragmentNoteListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true) // show menu in toolbar

        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(NoteListFragmentDirections.actionNotesListFragmentToAddEditNoteFragment(""))
        }

    }

    private fun logout() {
        removeAllCredentials(sharedPref, basicAuthInterceptor)

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.noteListFragment, true) // remove the noteListFragment from the back stack
            .build()
        findNavController().navigate(
            NoteListFragmentDirections.actionNotesListFragmentToAuthFragment(),
            navOptions
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_notes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miLogout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}