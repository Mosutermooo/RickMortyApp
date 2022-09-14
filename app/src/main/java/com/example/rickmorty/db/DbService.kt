package com.example.rickmorty.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.rickmorty.models.ApiCharacter


@Dao
interface DbService {

    @Delete
    suspend fun deleteFavoriteCharacter(character: ApiCharacter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteCharacter(character: ApiCharacter)

    @Query("SELECT * FROM characterTable where id LIKE :id")
    fun alreadyAddedToFavorite(id: Int) : ApiCharacter?

    @Query("SELECT * FROM characterTable")
    fun getAddedToFavoriteCharacters(): LiveData<List<ApiCharacter>>

    @Query("DELETE FROM characterTable where id = :id")
    suspend fun deleteSingleCharacter(id: Int)

    @Query("DELETE FROM characterTable")
    suspend fun deleteAllCharacters()




}