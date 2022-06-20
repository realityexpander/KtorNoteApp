package com.realityexpander.ktornoteapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotesTypeConverters {

    @TypeConverter // tells room to use this method to do conversion from json string
    fun fromList(list: List<String>): String { // converts a kotlin List<String> to a json string
        return Gson().toJson(list)
    }

    @TypeConverter // tells room to use this method to do conversion to a List<String>
    fun toList(string: String): List<String> {  // converts a json string to a kotlin List<String>
        return Gson().fromJson(string, object: TypeToken<List<String>>() {}.type) // used for generic type

        // return Gson().fromJson(string, Array<String>::class.java).toList()  // copilot suggested
    }
}