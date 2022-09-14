package com.example.rickmorty.data

import com.example.rickmorty.api.Retrofit
import com.example.rickmorty.db.DbService
import com.example.rickmorty.models.ApiCharacter

class CharacterRepository(
    private val dbService: DbService
) {

    suspend fun getMultipleCharacters(
        page: Int
    ) = Retrofit.apiService.getCharacters(page)

    suspend fun getMultipleEpisodes(
        ids: List<Int>
    ) = Retrofit.apiService.getMultipleEpisodes(ids)


    suspend fun saveFavoriteCharacter(
        character: ApiCharacter
    ) = dbService.addFavoriteCharacter(character)

    fun alreadyAddedToFavorite(
        id: Int
    ) = dbService.alreadyAddedToFavorite(id)

    fun getFavoriteCharacters() = dbService.getAddedToFavoriteCharacters()

    suspend fun deleteFromFavorite(
        character: ApiCharacter
    ) = dbService.deleteFavoriteCharacter(
        character
    )

    suspend fun deleteSingleCharacterFromDB(id: Int) = dbService.deleteSingleCharacter(id)
    suspend fun deleteAllCharacters() = dbService.deleteAllCharacters()

    suspend fun searchByName(query: String) = Retrofit.apiService.searchByName(query)


}