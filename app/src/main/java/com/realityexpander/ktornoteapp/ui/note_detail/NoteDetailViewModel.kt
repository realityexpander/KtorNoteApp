package com.realityexpander.ktornoteapp.ui.note_detail

import androidx.lifecycle.ViewModel
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    fun observeNoteId(noteId: String) = repository.observeNoteIdDb(noteId)
}