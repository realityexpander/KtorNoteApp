package com.realityexpander.ktornoteapp.ui.note_list

import androidx.lifecycle.*
import com.realityexpander.ktornoteapp.common.Event
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import com.realityexpander.ktornoteapp.ui.common.logoutFromViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    // forceUpdate is emitted upon load, then used to force update the list of notes.
    private val _forceUpdate = MutableLiveData<Unit>(Unit)

    private val _allNotes =
        _forceUpdate.switchMap {
            repository.getAllNotesCached()
                .asLiveData(viewModelScope.coroutineContext)
        }.switchMap { notes ->
            MutableLiveData(Event(notes))
        }
    val allNotes:LiveData<Event<Resource<List<NoteEntity>>>> = _allNotes


    fun logout(isLogoutDestructive: Boolean = false): Boolean {
        return logoutFromViewModel(isLogoutDestructive, repository)
    }

    // Called from pullToRefresh.
    fun syncAllNotes() =
        _forceUpdate.postValue(Unit)

    fun deleteNoteId(noteId: String) =
        viewModelScope.launch {
            repository.deleteNoteIdCached(noteId)
        }

    fun upsertNote(note: NoteEntity) =
        viewModelScope.launch {
            repository.upsertNoteCached(note)
        }

    fun deleteLocallyDeletedNoteId(noteId: String) =
        viewModelScope.launch {
            repository.deleteLocallyDeletedNoteIdDb(noteId)
        }

    ////// TESTING ///////
//    fun getOwnerIdForEmail(authEmail: String?): String? {
//        return runBlocking {
//            repository.getOwnerIdForEmail(authEmail)
//        }
//    }
}
