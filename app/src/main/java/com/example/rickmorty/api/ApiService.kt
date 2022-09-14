package com.example.rickmorty.api

import com.example.rickmorty.models.*
import com.example.rickmorty.utils.Constants
import com.example.rickmorty.utils.Constants.character
import com.example.rickmorty.utils.Constants.episode
import com.example.rickmorty.utils.Constants.gender
import com.example.rickmorty.utils.Constants.location
import com.example.rickmorty.utils.Constants.name
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET(character)
    suspend fun getCharacters(
        @Query(Constants.page) page: Int
    ): Response<CharacterResponse>


    @GET("$episode/{id}")
    suspend fun getMultipleEpisodes(
        @Path("id") id: List<Int>
    ) : Response<List<Episode>>

    @GET(episode)
    suspend fun getAllEpisodes(
        @Query(Constants.page) page: Int
    ) : Response<EpisodeResponse>

    @GET("$character/{id}")
    suspend fun getMultipleCharacters(
       @Path("id") id: List<Int>
    ): Response<List<ApiCharacter>>

    @GET(location)
    suspend fun getAllLocations(
        @Query(Constants.page) page: Int
    ) : Response<LocationResponse>


    @GET(character)
    suspend fun searchByName(
        @Query(name) queryName: String
    ) : Response<CharacterResponse>

    @GET(episode)
    suspend fun searchEpisodeByName(
        @Query(name) queryName: String
    ) : Response<EpisodeResponse>


    @GET(location)
    suspend fun searchLocationByName(
        @Query(name) queryName: String
    ) : Response<LocationResponse>



}