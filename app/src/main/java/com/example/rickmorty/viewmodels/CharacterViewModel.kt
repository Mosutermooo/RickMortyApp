package com.example.rickmorty.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rickmorty.data.CharacterRepository
import com.example.rickmorty.db.AppDatabase
import com.example.rickmorty.models.CharacterResponse
import com.example.rickmorty.models.ApiCharacter
import com.example.rickmorty.models.Episode
import com.example.rickmorty.utils.CheckNetwork
import com.example.rickmorty.utils.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response
import java.net.SocketTimeoutException

class CharacterViewModel(
    application: Application
) : AndroidViewModel(application) {

    val characters : MutableLiveData<Resource<CharacterResponse>> = MutableLiveData()
    val searchedCharacters : MutableLiveData<Resource<CharacterResponse>> = MutableLiveData()
    val multipleEpisodes : MutableLiveData<Resource<List<Episode>>> = MutableLiveData()
    var characterPage : Int = 1
    var repository: CharacterRepository
    private var characterResponse : CharacterResponse? = null


    init {
        val dao = AppDatabase.getDatabase(application).dbServiceDao()
        repository = CharacterRepository(dao)
    }

    fun getMultipleCharacters() = viewModelScope.launch {
        safeGetAllCharactersCall()
    }

    private suspend fun safeGetAllCharactersCall() {
        try {
            characters.postValue(Resource.Loading())
            val response = repository.getMultipleCharacters(characterPage)
            characters.postValue(handleMultipleCharacterResponse(response))
        }catch (t: Throwable){
            when(t){
                is IOException -> {
                    characters.postValue(Resource.Error("IOException"))
                }
                else -> characters.postValue(Resource.Error(t.message.toString()))
            }
        }catch (socket: SocketTimeoutException){
            characters.postValue(Resource.Error("SocketError"))
        }
    }

    private fun handleMultipleCharacterResponse(response: Response<CharacterResponse>): Resource<CharacterResponse>{
        if(response.isSuccessful){
            response.body()?.let {apiResponse ->
                characterPage++
                if(characterPage >= 43){
                    characterPage = 42
                }else{
                    if(characterResponse == null){
                        characterResponse = apiResponse
                    }else{
                        val oldCharacters = characterResponse?.results
                        val newCharacters = apiResponse.results
                        oldCharacters?.addAll(newCharacters)
                    }
                }
                return Resource.Success(characterResponse ?: apiResponse)
            }

        }
        return Resource.Error(response.message().toString())
    }



    fun getMultipleEpisodes(character: ApiCharacter){
        val episodeIds = character.episode.map {
            it.split("/").last().toInt()
        }

        viewModelScope.launch {
           val response = repository.getMultipleEpisodes(episodeIds)
           multipleEpisodes.postValue(Resource.Loading())
           multipleEpisodes.postValue(handleMultipleEpisodeResponse(response))
        }

    }

    private fun handleMultipleEpisodeResponse(response: Response<List<Episode>>) : Resource<List<Episode>> {
        if(response.isSuccessful){
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }

    fun search(query: String?) = viewModelScope.launch {
        if(query != null){
            val response = repository.searchByName(query)
            searchedCharacters.postValue(Resource.Loading())
            if (response.isSuccessful){
                response.body()?.let {
                    searchedCharacters.postValue(Resource.Success(it))
                }
            }else{
                searchedCharacters.postValue(Resource.Error(response.message().toString()))
            }

        }
    }

    fun saveFavoriteCharacter(character: ApiCharacter) = viewModelScope.launch {
        repository.saveFavoriteCharacter(character)
    }

    fun alreadyAddedToFavorite(id:Int) = repository.alreadyAddedToFavorite(id)

    fun getFavoriteCharacters() = repository.getFavoriteCharacters()

    fun deleteFavoriteCharacter(character: ApiCharacter) = viewModelScope.launch {
        repository.deleteFromFavorite(character)
    }

    fun deleteSingleCharacter(id: Int) = viewModelScope.launch {
        repository.deleteSingleCharacterFromDB(id)
    }

    fun deleteAllCharacters() = viewModelScope.launch {
        repository.deleteAllCharacters()
    }





}