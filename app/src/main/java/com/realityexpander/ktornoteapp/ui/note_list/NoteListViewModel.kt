package com.realityexpander.ktornoteapp.ui.note_list

import androidx.lifecycle.*
import com.realityexpander.ktornoteapp.common.Event
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _forceUpdate = MutableLiveData<Boolean>(false)
    
    private val _allNotes = _forceUpdate.switchMap {
        noteRepository.getAllNotesCached()
            .asLiveData(viewModelScope.coroutineContext)
    }.switchMap { notes ->
        MutableLiveData(Event(notes))
    }
    
    val allNotes:LiveData<Event<Resource<List<NoteEntity>>>> = _allNotes
}
