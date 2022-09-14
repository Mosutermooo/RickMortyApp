package com.example.rickmorty.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "characterTable")
data class ApiCharacter(
    @PrimaryKey(autoGenerate = true)
    val characterId: Int? = null,
    val created: String,
    val episode: List<String>,
    val gender: String,
    val id: Int,
    val image: String,
    val location: Location,
    val name: String,
    val origin: Origin,
    val species: String,
    val status: String,
    val type: String,
    val url: String
): Serializable