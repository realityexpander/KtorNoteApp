package com.realityexpander.ktornoteapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int)

    @Query("DELETE FROM notes WHERE isSynced = 1") // Delete all synced notes
    suspend fun deleteAllSyncedNotes()

    // returning LiveData excludes the need to declare this a suspend func
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observerNoteById(noteId: String): LiveData<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?

    // returning flow excludes the need to declare this a suspend func
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getAllUnsyncedNotes(): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

}