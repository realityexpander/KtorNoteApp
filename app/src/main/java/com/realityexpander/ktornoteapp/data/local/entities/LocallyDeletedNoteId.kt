package com.realityexpander.ktornoteapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tracks the note ID's deleted locally (when offline)
//  that need to be deleted on the server at next sync.

@Entity(tableName = "locally_deleted_note_ids")
data class LocallyDeletedNoteId(
    @PrimaryKey(autoGenerate = false)
    val deletedNoteId: String
)
