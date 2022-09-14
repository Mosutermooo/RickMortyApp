package com.example.rickmorty.db

import androidx.room.TypeConverter
import com.example.rickmorty.models.Location
import com.example.rickmorty.models.Origin
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class Converters {

    @TypeConverter
    fun fromLocation(location: Location): String {
        return location.name
    }

    @TypeConverter
    fun toLocation(name: String): Location {
        return Location(name, name)
    }

    @TypeConverter
    fun fromOrigin(origin: Origin): String {
        return origin.name
    }

    @TypeConverter
    fun toOrigin(name: String): Origin {
        return Origin(name, name)
    }

    @TypeConverter
    fun fromListOfImages(episodes: List<String>): String {
        return Gson().toJson(episodes)
    }

    @TypeConverter
    fun toListOfImages(episode: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>>(episode, listType)   }


}