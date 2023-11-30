package com.example.examen

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaceDao {
    @Query("SELECT * FROM place")
    fun getAllPlaces(): LiveData<List<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlace(place: Place)

    @Query("SELECT * FROM place WHERE id = :placeId")
    fun getPlaceById(placeId: String?): LiveData<Place>

    @Delete
    suspend fun deletePlace(place: Place)

    @Query("UPDATE place SET imageUrl = :imageUrl WHERE id = :placeId")
    suspend fun updatePlaceImage(placeId: String, imageUrl: String)
}
