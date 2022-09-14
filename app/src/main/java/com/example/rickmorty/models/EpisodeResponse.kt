package com.example.rickmorty.models

data class EpisodeResponse(
    val info: InfoEpisode,
    val results: ArrayList<Episode>
)