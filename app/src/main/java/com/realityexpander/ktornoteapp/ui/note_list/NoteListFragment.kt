package com.realityexpander.ktornoteapp.ui.note_list

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.*
import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
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

    // Helper to prevent conflict on swiping left/right on item and swipe to refresh
    private val isSwipingRecyclerViewItem = MutableLiveData(false)

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
        setupNoteListAdapter()

        subscribeToObservers()
        setupSwipeToRefreshLayout()
        setupFAB()


//        ///// TESTING /////
//        val authEmail = sharedPref.getString(
//            Constants.ENCRYPTED_SHARED_PREF_KEY_LOGGED_IN_EMAIL, null
//        )
//        lifecycleScope.launchWhenStarted {
//            val authUserId = viewModel.getOwnerIdForEmail(authEmail) ?: "unknown user id"
//            showSnackbar("User ID: $authUserId")
//        }

    }

    // Set the item click listener for the Notes List items
    private fun setupNoteListAdapter() {
        noteListAdapter.setOnItemClickListener { note ->
            if (note.id.isNotBlank()) {
                findNavController()
                    .navigate(
                        NoteListFragmentDirections
                            .actionNotesListFragmentToNoteDetailFragment(note.id)
                    )

                return@setOnItemClickListener
            }

            showSnackbar("Note ID is empty")
        }
    }

    private fun setupFAB() {
        binding.fabAddNote.setOnClickListener {
            findNavController()
                .navigate(
                    NoteListFragmentDirections
                        .actionNotesListFragmentToAddEditNoteFragment("")
                )
        }
    }

    // respond to swipe to refresh
    private fun setupSwipeToRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.syncAllNotes()
        }
    }

    private fun subscribeToObservers() {

        // Observe and respond to Events from the ViewModel
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

                        event.getContentOnlyOnce()?.let { errorResource ->
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

        // Disable the "swipe to refresh" when swiping left/right on the recycler view item
        isSwipingRecyclerViewItem.observe(viewLifecycleOwner) { isSwiping ->
            binding.swipeRefreshLayout.isEnabled = !isSwiping
        }
    }

    private fun setupRecyclerView() = binding.rvNotes.apply {
        noteListAdapter = NoteListAdapter()
        adapter = noteListAdapter
        layoutManager = LinearLayoutManager(requireContext())
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(this)
    }

    private val itemTouchHelperCallback =
        object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.layoutPosition
            val note = noteListAdapter.notes[position]
            viewModel.deleteNoteId(note.id)

            if (direction == ItemTouchHelper.LEFT) println("Swipe direction: LEFT")
            if (direction == ItemTouchHelper.RIGHT) println("Swipe direction: RIGHT")


            // Add snackbar to show undo action
            Snackbar.make(
                binding.root,
                "Note deleted",
                Snackbar.LENGTH_LONG
            ).setAction("UNDO") {
                viewModel.upsertNote(note)
                viewModel.deleteLocallyDeletedNoteId(note.id) // just in case network call failed
            }.show()
        }

        // Check if user is swiping left/right on RecyclerView item.
        // Also draw the "swipe to delete" areas on left/right of the item.
        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

            drawLeftRightHelpIndicators(c, viewHolder, dX, actionState)

            if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                isSwipingRecyclerViewItem.postValue(isCurrentlyActive)
            }

        }
    }

    private fun logout(isLogoutDestructive: Boolean = false) {

        logoutFromFragment(
            isLogoutDestructive = isLogoutDestructive,
            isViewModelLogoutSuccessful = { isDestructive -> viewModel.logout(isDestructive) },
            sharedPref = sharedPref,
            basicAuthInterceptor = basicAuthInterceptor
        ) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.noteListFragment, true) // remove the noteListFragment from the back stack
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

    private fun drawLeftRightHelpIndicators(
        c: Canvas,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        actionState: Int
    ) {
        var startX = 0f
        var endX = 0f
        var textAlignment = Paint.Align.LEFT
        var textEndXAdjustment = 0f
        val textFontSize = 60f
        val numTextLines = 3
        val lineSpacing = 65f
        val textPadding = 30f

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX > 0) {
                // Swiping to the right
                startX = viewHolder.itemView.left.toFloat()
                endX = viewHolder.itemView.left.toFloat() + dX
                textAlignment = Paint.Align.RIGHT
                textEndXAdjustment = -40f - (textPadding / 2)
            } else {
                // Swiping to the left
                startX = viewHolder.itemView.right.toFloat()
                endX = viewHolder.itemView.right.toFloat() + dX
                textAlignment = Paint.Align.LEFT
            }
        }

        fun drawTextLine(text: String, c: Canvas, x: Float, y: Float) {
            c.drawText(text, x, y,
                Paint().apply {
                    color = Color.WHITE
                    strokeWidth = 2f
                    fontMetricsInt.let {
                        textSize = textFontSize
                        isFakeBoldText = true
                        textAlign = textAlignment
                    }
                })
        }

        // save canvas state
        c.save()

        // Fill for "swipe left/right" indicator background
        c.drawRect(
            startX, //viewHolder.itemView.left.toFloat(),
            viewHolder.itemView.top.toFloat(),
            endX, //viewHolder.itemView.left.toFloat() + dX,
            viewHolder.itemView.bottom.toFloat(),
            Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            }
        )

        // Draw "swipe left/right" indicator text

        var y = ( // find the vertical center for the text block
                (viewHolder.itemView.bottom.toFloat() - viewHolder.itemView.top.toFloat()) / 2f
                ) - lineSpacing + (textFontSize / (numTextLines + 1))
        drawTextLine(
            "swipe", c,
            endX + textPadding + textEndXAdjustment,
            viewHolder.itemView.top.toFloat() + y,
        )
        y += lineSpacing
        drawTextLine(
            "left/right", c,
            endX + textPadding + textEndXAdjustment,
            viewHolder.itemView.top.toFloat() + y,
        )
        y += lineSpacing
        drawTextLine(
            "to delete", c,
            endX + textPadding + textEndXAdjustment,
            viewHolder.itemView.top.toFloat() + y,
        )

        c.restore()

//        Line Draw Test
//        c.drawLine(
//            viewHolder.itemView.right.toFloat(),
//            viewHolder.itemView.top.toFloat(),
//            viewHolder.itemView.right.toFloat() + dX,
//            viewHolder.itemView.bottom.toFloat(),
//            Paint().apply {
//                color = Color.WHITE
//                strokeWidth = 2f
//            }
//        )
    }

}