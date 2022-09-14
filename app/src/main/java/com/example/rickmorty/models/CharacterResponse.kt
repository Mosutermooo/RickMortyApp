package com.example.rickmorty.models

data class CharacterResponse(
    val info: Info,
    val results: ArrayList<ApiCharacter>
)