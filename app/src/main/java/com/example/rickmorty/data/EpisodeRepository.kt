package com.example.rickmorty.data

import com.example.rickmorty.api.Retrofit
import com.example.rickmorty.db.DbService

class EpisodeRepository (
    dbService: DbService
        ) {

    suspend fun getAllEpisodes(
        page: Int
    ) = Retrofit.apiService.getAllEpisodes(page)

    suspend fun getMultipleCharacters(
        id: List<Int>
    ) = Retrofit.apiService.getMultipleCharacters(id)

    suspend fun searchByName(query: String) = Retrofit.apiService.searchEpisodeByName(query)
}