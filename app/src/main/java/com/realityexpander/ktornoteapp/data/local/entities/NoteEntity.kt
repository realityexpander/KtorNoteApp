package com.realityexpander.ktornoteapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import java.text.SimpleDateFormat
import java.util.*

// This NoteEntity is used for database, network and UI (TODO should be separated!)

@Entity(tableName = "notes") // for Room
data class NoteEntity(
    @PrimaryKey(autoGenerate = false)  // don't let Room generate id
    var id: String = UUID.randomUUID().toString(),  // id generated here

    val title: String,
    val content: String,
    val date: String,
    val dateMillis: Long = 0,
    val owners: List<String>,  // must use @TypeConverter to convert List<String> to json string
    val color: String,
    val createdAt: Long = 0,  // milliseconds since epoch
    val updatedAt: Long = 0,  // milliseconds since epoch

    @Expose( // Ignore for retrofit, don't serialize/deserialize
        deserialize = false,
        serialize = false
    )
    var isSynced: Boolean = false,
)

typealias NoteEntities = List<NoteEntity>

// Standard date format for Notes
fun millisToDateString(longMillis: Long): String {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy, hh:mm a", Locale.getDefault())

    return dateFormat.format(longMillis)
}