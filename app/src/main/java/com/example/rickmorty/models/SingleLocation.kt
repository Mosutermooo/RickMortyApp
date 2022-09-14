package com.example.rickmorty.models

import java.io.Serializable

data class SingleLocation(
    val created: String,
    val dimension: String,
    val id: Int,
    val name: String,
    val residents: List<String>,
    val type: String,
    val url: String
): Serializable