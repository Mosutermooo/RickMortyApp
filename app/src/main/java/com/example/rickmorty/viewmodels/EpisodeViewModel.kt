package com.example.rickmorty.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rickmorty.data.EpisodeRepository
import com.example.rickmorty.db.AppDatabase
import com.example.rickmorty.models.ApiCharacter
import com.example.rickmorty.models.Episode
import com.example.rickmorty.models.EpisodeResponse
import com.example.rickmorty.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Query

class EpisodeViewModel (application: Application): AndroidViewModel(application) {


    val episode : MutableLiveData<Resource<EpisodeResponse>> =  MutableLiveData()
    val searchedEpisode : MutableLiveData<Resource<EpisodeResponse>> =  MutableLiveData()
    val multipleCharacters : MutableLiveData<Resource<List<ApiCharacter>>> =  MutableLiveData()
    private var episodeResponse: EpisodeResponse? = null

    private var repository: EpisodeRepository
    var currentEpisodePage: Int = 1
    private var pages: Int? = null

    init {
        val dao = AppDatabase.getDatabase(application).dbServiceDao()
        repository = EpisodeRepository(dao)
    }

    fun getAllEpisode() = viewModelScope.launch {
        if(currentEpisodePage != 4){
            episode.postValue(Resource.Loading())
            val response = repository.getAllEpisodes(currentEpisodePage)
            episode.postValue(handleEpisodeResponse(response))
        }


    }

    private fun handleEpisodeResponse(response: Response<EpisodeResponse>): Resource<EpisodeResponse>? {
        if(response.isSuccessful){
            if(currentEpisodePage != 4){
                response.body()?.let {apiResponse->
                    currentEpisodePage++
                    pages = apiResponse.info.pages
                    if(episodeResponse == null){
                        episodeResponse =  apiResponse
                    }else{
                        val oldEpisodes = episodeResponse?.results
                        val newEpisodes = apiResponse.results
                        oldEpisodes?.addAll(newEpisodes)

                        Log.e("page after get", "$currentEpisodePage")
                    }
                    return  Resource.Success(episodeResponse ?: apiResponse)
                }
            }
        }
        return Resource.Error(response.errorBody().toString())
    }

    fun getMultipleCharacters(episode: Episode) = viewModelScope.launch {
        val characterIds = episode.characters.map {
            it.split("/").last().toInt()
        }
        multipleCharacters.postValue(Resource.Loading())
        val response = repository.getMultipleCharacters(characterIds)
        multipleCharacters.postValue(handleMultipleCharacterResponse(response))
    }

    private fun handleMultipleCharacterResponse(response: Response<List<ApiCharacter>>): Resource<List<ApiCharacter>>? {
        if(response.isSuccessful){
            response.body()?.let {characters ->
                return Resource.Success(characters)
            }
        }
        return Resource.Error(response.errorBody().toString())
    }


    fun searchEpisodes(query: String) = viewModelScope.launch {
        val response = repository.searchByName(query)
        if(response.isSuccessful){
            response.body()?.let {
                searchedEpisode.postValue(Resource.Success(it))
            }
        }
    }

}