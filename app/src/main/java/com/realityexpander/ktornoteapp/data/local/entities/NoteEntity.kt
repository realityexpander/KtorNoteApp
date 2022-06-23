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
    val date: String, //Long,
    // val dateLong: Long,  // TODO add this
    val owners: List<String>,  // must use @TypeConverter to convert the list to a json string
    val color: String,

    @Expose(
        deserialize = false,
        serialize = false
    ) // Ignore for retrofit, not serialized/deserialized
    var isSynced: Boolean = false,
    val dateMillis: Long
)

typealias NoteEntityList = List<NoteEntity>

fun millisToDateString(longMillis: Long): String {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy, HH:mm", Locale.getDefault())

    return dateFormat.format(longMillis)
}