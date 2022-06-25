package com.realityexpander.ktornoteapp.ui.note_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.ktornoteapp.common.Event
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.data.remote.responses.SimpleResponseWithData
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _addOwnerToNoteStatus = MutableLiveData<Event<Resource<SimpleResponseWithData<NoteEntity>>>>()
    val addOwnerToNoteStatus: LiveData<Event<Resource<SimpleResponseWithData<NoteEntity>>>> =
        _addOwnerToNoteStatus

    fun addOwnerEmailToNoteId(ownerEmail: String, noteId: String) {
        _addOwnerToNoteStatus.postValue( Event(Resource.loading()) )

        viewModelScope.launch {
            val result =
                repository.addOwnerByEmailToNoteIdApi(ownerEmail, noteId)

            if (result.status == Status.SUCCESS) {
                // Update the local DB with the newly added ownerId
                result.data?.data?.let { updatedNote ->
                    repository.upsertNoteDb(updatedNote)
                }
            }

            _addOwnerToNoteStatus.postValue( Event(result) )
        }
    }

    fun getEmailForOwnerId(ownerId: String): String {
        return runBlocking {
            repository.getEmailForOwnerIdApi(ownerId) ?: "Unknown OwnerID"
        }
    }

    fun observeNoteId(noteId: String) = repository.observeNoteIdDb(noteId)
}