package com.realityexpander.ktornoteapp.ui.note_list

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.data.remote.BasicAuthInterceptor
import com.realityexpander.ktornoteapp.databinding.FragmentNoteListBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment
import com.realityexpander.ktornoteapp.ui.adapters.NoteListAdapter
import com.realityexpander.ktornoteapp.ui.common.logoutFromFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NoteListFragment: BaseFragment(R.layout.fragment_note_list) {

    private val viewModel: NoteListViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private lateinit var noteListAdapter: NoteListAdapter

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

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER

        setupRecyclerView()
        subscribeToObservers()

        // Set the item click listener for the Notes List items
        noteListAdapter.setOnItemClickListener { note ->
            if (note.id.isNotBlank()) {
                findNavController().navigate(
                    NoteListFragmentDirections.actionNotesListFragmentToNoteDetailFragment(note.id),
                )

                return@setOnItemClickListener
            }

            showSnackbar("Note ID is empty")
        }

        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(NoteListFragmentDirections.actionNotesListFragmentToAddEditNoteFragment(""))
        }


//        ///// TEST /////
//        val authEmail = sharedPref.getString(
//            Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL, null
//        )
//        lifecycleScope.launchWhenStarted {
//            val authUserId = viewModel.getOwnerIdForEmail(authEmail) ?: "unknown user id"
//            showSnackbar("User ID: $authUserId")
//        }

    }

    private fun subscribeToObservers() {
        viewModel.allNotes.observe(viewLifecycleOwner) { eventNullable ->

            eventNullable?.let { event ->
                val result = event.peekContent()

                when (result.status) {
                    Status.SUCCESS -> {
                        binding.swipeRefreshLayout.isRefreshing = false

                        // Should use a special XML item for this (TODO)
                        if (result.data.isNullOrEmpty()) {

                            // Show "empty list" note item
                            noteListAdapter.notes = listOf(
                                NoteEntity(
                                    id = "",
                                    title = "No notes found",
                                    content = "",
                                    date = "Add a note to get started",
                                    owners = listOf(""),
                                    color = "#FFFFFF",
                                    dateMillis = 0
                                )
                            )

                            return@let
                        }

                        noteListAdapter.notes = result.data
                    }
                    Status.ERROR -> {
                        binding.swipeRefreshLayout.isRefreshing = false

                        event.getContentIfNotHandled()?.let { errorResource ->
                            errorResource.message?.let { message ->
                                showSnackbar(message)
                            }
                        }

                        // Even with error, use stale data if available
                        result.data?.let { staleNotes ->
                            noteListAdapter.notes = staleNotes
                        }
                    }
                    Status.LOADING -> {
                        binding.swipeRefreshLayout.isRefreshing = true

                        // While loading, use the stale data if available
                        result.data?.let { staleNotes ->
                            noteListAdapter.notes = staleNotes
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.rvNotes.apply {
        noteListAdapter = NoteListAdapter()
        adapter = noteListAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun logout(isLogoutDestructive: Boolean = false) {
//        val isLogoutSafe = viewModel.logout()
//
//        if (isLogoutDestructive || isLogoutSafe) {
//
//            showSnackbar("Logging out...")
//
//            viewModel.logout(isLogoutDestructive = true)
//            removeAllCredentials(sharedPref, basicAuthInterceptor)
//
//            val navOptions = NavOptions.Builder()
//                .setPopUpTo(
//                    R.id.noteListFragment,
//                    true
//                ) // remove the noteListFragment from the back stack
//                .build()
//            findNavController().navigate(
//                NoteListFragmentDirections.actionNotesListFragmentToAuthFragment(),
//                navOptions
//            )
//        } else {
//            showLogoutWarningDialog()
//        }

        logoutFromFragment(
            isLogoutDestructive = isLogoutDestructive,
            viewModelLogout = { isDestructive -> viewModel.logout(isDestructive) },
            sharedPref = sharedPref,
            basicAuthInterceptor = basicAuthInterceptor
        ) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(
                    R.id.noteListFragment,
                    true
                ) // remove the noteListFragment from the back stack
                .build()
            findNavController().navigate(
                NoteListFragmentDirections.actionNotesListFragmentToAuthFragment(),
                navOptions
            )
        }
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

//    // show dialog to confirm logout
//    private fun showLogoutWarningDialog() {
//        val dialog = AlertDialog.Builder(requireContext())
//            .setTitle("Logout")
//            .setMessage("There are unsynced/unsaved notes that will be deleted if you log out now.\n\nAre you sure you want to logout?")
//            .setPositiveButton("Yes") { _, _ -> logout(isLogoutDestructive = true) }
//            .setNegativeButton("No") { _, _ -> }
//            .create()
//        dialog.show()
//    }

}