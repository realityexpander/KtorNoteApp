package com.realityexpander.ktornoteapp.ui.note_add_edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.ktornoteapp.common.Event
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class NoteAddEditViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _note = MutableLiveData<Event<Resource<NoteEntity>>>()
    val note: MutableLiveData<Event<Resource<NoteEntity>>> = _note

    // Since we update the note when the user presses the back button (not a save button),
    //   we run the upsert in the GlobalScope to keep it alive after the fragment is destroyed.
    // This ensures the upsert will run even if the user presses the back button.
    @OptIn(DelicateCoroutinesApi::class)
    fun upsertNote(note: NoteEntity) = GlobalScope.launch {
        repository.upsertNoteCached(note)
    }

    fun getNoteById(id: String) = viewModelScope.launch {
        _note.postValue(Event(Resource.loading(data = null)))

        val note = repository.getNoteByIdDb(id)
        note?.let {
            _note.postValue(Event(Resource.success(data = it)))
        } ?: _note.postValue(Event(Resource.error(data = null, message = "Error")))


    }

    fun getOwnerIdForEmail(authEmail: String?): String? {
        return runBlocking {
            repository.getOwnerIdForEmail(authEmail)
        }
    }

}