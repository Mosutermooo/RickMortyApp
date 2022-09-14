package com.example.rickmorty.data

import com.example.rickmorty.api.Retrofit
import com.example.rickmorty.db.DbService
import retrofit2.http.Query

class LocationRepository (
    private val dbService: DbService
) {

    suspend fun getAllLocations(page: Int) = Retrofit.apiService.getAllLocations(page)
    suspend fun getMultipleLocations(ids: List<Int>) = Retrofit.apiService.getMultipleCharacters(ids)
    suspend fun searchByName(query: String) = Retrofit.apiService.searchLocationByName(query)


}