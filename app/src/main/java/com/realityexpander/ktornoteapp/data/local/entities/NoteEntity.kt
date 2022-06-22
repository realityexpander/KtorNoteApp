package com.realityexpander.ktornoteapp.data.local.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import java.util.*

@Entity(tableName = "notes") // for Room
data class NoteEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),  // id generated here

    val title: String,
    val content: String,
    val date: String, //Long,
    val owners : List<String>,  // must use TypeConverter to convert the list to a json string
    val color: String,

    @Expose(deserialize = false, serialize = false) // Ignored by retrofit and not serialized/deserialized
    val isSynced: Boolean = false
)

typealias NoteEntityList = List<NoteEntity>