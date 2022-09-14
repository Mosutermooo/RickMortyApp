package com.example.rickmorty.models

import java.io.Serializable

data class Origin(
    val name: String,
    val url: String
) : Serializable