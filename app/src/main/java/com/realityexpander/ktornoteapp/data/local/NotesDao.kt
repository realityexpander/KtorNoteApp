package com.realityexpander.ktornoteapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.realityexpander.ktornoteapp.data.local.entities.LocallyDeletedNoteId
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    //// NOTES ////

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteId(noteId: String): Unit

    @Query("DELETE FROM notes WHERE isSynced = 1") // Delete all synced notes
    suspend fun deleteAllSyncedNotes(): Unit

    // returning LiveData excludes the need to declare this a suspend func
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeNoteId(noteId: String): LiveData<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteId(noteId: String): NoteEntity?

    // returning flow excludes the need to declare this a suspend func
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getAllUnsyncedNotes(): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes(): Unit


    //// LOCALLY DELETED NOTE IDs ////

    @Query("SELECT * FROM locally_deleted_note_ids")
    suspend fun getAllLocallyDeletedNoteIds(): List<String>

    @Query("DELETE FROM locally_deleted_note_ids WHERE deletedNoteId = :locallyDeletedNoteId")
    suspend fun deleteLocallyDeletedNoteId(locallyDeletedNoteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // gets the table to use from the type of the entity (ie: LocallyDeletedNoteId)
    suspend fun insertLocallyDeletedNoteId(locallyDeletedNoteId: LocallyDeletedNoteId)

}