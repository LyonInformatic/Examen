package com.example.examen

import androidx.lifecycle.LiveData

class PlaceRepository(private val placeDao: PlaceDao) {
    val allPlaces: LiveData<List<Place>> = placeDao.getAllPlaces()

    suspend fun insertOrUpdatePlace(place: Place) {

        placeDao.insertOrUpdatePlace(place)
    }

    fun getPlaceById(placeId: String?): LiveData<Place> {
        return placeDao.getPlaceById(placeId)
    }
    suspend fun deletePlace(place: Place) {
        placeDao.deletePlace(place)
    }
    suspend fun updatePlaceImage(placeId: String, imageUrl: String) {
        placeDao.updatePlaceImage(placeId, imageUrl)
    }
}
