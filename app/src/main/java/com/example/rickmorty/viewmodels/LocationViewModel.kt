package com.example.rickmorty.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.rickmorty.data.LocationRepository
import com.example.rickmorty.db.AppDatabase
import com.example.rickmorty.models.ApiCharacter
import com.example.rickmorty.models.LocationResponse
import com.example.rickmorty.models.SingleLocation
import com.example.rickmorty.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Query

class LocationViewModel(
    application: Application
) : AndroidViewModel(application) {

    val locations : MutableLiveData<Resource<LocationResponse>> = MutableLiveData()
    val searchedLocations : MutableLiveData<Resource<LocationResponse>> = MutableLiveData()
    private val _residents = MutableLiveData<Resource<List<ApiCharacter>>>()
    val residents : LiveData<Resource<List<ApiCharacter>>>
    get() = _residents

    private val repository: LocationRepository
    var currentLocationPage: Int = 1
    var locationPage : Int? = null
    private var locationResponse: LocationResponse? = null

    init {
        val dbService = AppDatabase.getDatabase(application).dbServiceDao()
        repository = LocationRepository(dbService)
    }


    fun getAllLocations() = viewModelScope.launch {
        if(currentLocationPage != 8){
            val response = repository.getAllLocations(currentLocationPage)
            locations.postValue(Resource.Loading())
            locations.postValue(handleGetAllLocationsResponse(response))
        }

    }

    private fun handleGetAllLocationsResponse(response: Response<LocationResponse>): Resource<LocationResponse> {
        if(response.isSuccessful) {
            response.body()?.let { apiResponse ->
                currentLocationPage++
                locationPage = apiResponse.info.pages
                if(locationResponse == null){
                    locationResponse = apiResponse
                }else{
                    val oldLocations = locationResponse?.results
                    val newLocations = apiResponse.results
                    oldLocations?.addAll(newLocations)
                }
                return Resource.Success(locationResponse ?: apiResponse)
            }
        }
        return Resource.Error(response.message().toString())
    }

    fun multipleLocations(location: SingleLocation){
        val residentIds = location.residents.map {residentLink ->
            residentLink.split("/").last().toInt()
        }

      if(residentIds.isNotEmpty()){
          viewModelScope.launch {
              val response = repository.getMultipleLocations(residentIds)
              _residents.postValue(Resource.Loading())
              if(response.isSuccessful){
                  _residents.postValue(Resource.Success(response.body()))
              }else{
                  _residents.postValue(Resource.Error(response.message()))
              }
          }
      }else{
          _residents.postValue(Resource.NoResidents())
      }
    }

    fun searchByName(query: String) = viewModelScope.launch {
        val response = repository.searchByName(query)
        searchedLocations.postValue(Resource.Loading())
        if(response.isSuccessful){
            response.body()?.let {
                searchedLocations.postValue(Resource.Success(it))
            }
        }else{
            searchedLocations.postValue(Resource.Error(response.message().toString()))
        }
    }


}