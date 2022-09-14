package com.example.rickmorty.models

data class LocationResponse(
    val info: LocationInfo,
    val results: ArrayList<SingleLocation>
)