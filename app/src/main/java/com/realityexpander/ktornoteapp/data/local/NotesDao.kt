package com.realityexpander.ktornoteapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int)

    @Query("DELETE FROM notes WHERE isSynced = 1") // Delete all synced notes
    suspend fun deleteAllSyncedNotes()

    // returning live data excludes the need to make this a suspend func
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observerNoteById(noteId: String): LiveData<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getAllUnsyncedNotes(): List<NoteEntity>

}