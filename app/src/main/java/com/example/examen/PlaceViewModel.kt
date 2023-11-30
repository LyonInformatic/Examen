package com.example.examen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PlaceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlaceRepository
    val allPlaces: LiveData<List<Place>>

    init {
        val placeDao = AppDatabase.getDatabase(application).placeDao()
        repository = PlaceRepository(placeDao)
        allPlaces = repository.allPlaces
    }

    // Función para insertar o actualizar un lugar
    fun insertOrUpdatePlace(place: Place) {
        viewModelScope.launch {
            repository.insertOrUpdatePlace(place)
        }
    }

    // Función para obtener un lugar por ID (si es necesario)
    fun getPlaceById(placeId: String?): LiveData<Place> {
        return repository.getPlaceById(placeId)
    }
    fun deletePlace(place: Place) {
        viewModelScope.launch {
            repository.deletePlace(place)
        }
    }
    fun updatePlaceImage(placeId: String, imageUrl: String) {
        viewModelScope.launch {
            repository.updatePlaceImage(placeId, imageUrl)
        }
    }
}
