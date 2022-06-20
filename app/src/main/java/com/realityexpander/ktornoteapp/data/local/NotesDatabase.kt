package com.realityexpander.ktornoteapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity

@Database(entities = [NoteEntity::class], version = 1)
@TypeConverters(TypeConverters::class)
abstract class NotesDatabase: RoomDatabase() {

    abstract fun notesDao(): NotesDao

}