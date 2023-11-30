package com.example.examen

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Place(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    var order: Int,
    var imageUrl: String,
    var latitude: Double,
    var longitude: Double,
    val accommodationCost: Double,
    val transportationCost: Double,
    var additionalComments: String
)
