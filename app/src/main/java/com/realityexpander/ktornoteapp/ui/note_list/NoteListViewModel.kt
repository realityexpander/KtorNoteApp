package com.realityexpander.ktornoteapp.ui.note_list

import androidx.lifecycle.*
import com.realityexpander.ktornoteapp.common.Event
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import com.realityexpander.ktornoteapp.ui.common.logoutFromViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData<Boolean>(false)
    
    private val _allNotes = _forceUpdate.switchMap {
        repository.getAllNotesCached()
            .asLiveData(viewModelScope.coroutineContext)
    }.switchMap { notes ->
        MutableLiveData(Event(notes))
    }
    
    val allNotes:LiveData<Event<Resource<List<NoteEntity>>>> = _allNotes


    fun logout(isLogoutDestructive: Boolean = false): Boolean {
//        return runBlocking {
//
//            // Only clear the DB if there are no unsynced notes, or
//            // the user wants to logout destructively.
//            if (logoutDestructively || repository.getUnsyncedNotesDb().isEmpty()) {
//                repository.deleteAllNotesDb()
//
//                return@runBlocking true
//            }
//
//            false
//        }
        return logoutFromViewModel(isLogoutDestructive, repository)
    }


    ////// TESTING ///////
//    fun getOwnerIdForEmail(authEmail: String?): String? {
//        return runBlocking {
//            repository.getOwnerIdForEmail(authEmail)
//        }
//    }
}
