package com.realityexpander.ktornoteapp.ui.note_list

import androidx.lifecycle.*
import com.realityexpander.ktornoteapp.common.Event
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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




    ////// TESTING ///////
//    fun getOwnerIdForEmail(authEmail: String?): String? {
//        return runBlocking {
//            repository.getOwnerIdForEmail(authEmail)
//        }
//    }
}
