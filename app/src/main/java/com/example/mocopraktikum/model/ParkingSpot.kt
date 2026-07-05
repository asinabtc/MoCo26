package com.example.mocopraktikum.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_spots")
data class ParkingSpot(
    @PrimaryKey
    val id: String,
    val title: String,
    val street: String = "",
    val zipCode: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distance: String,
    val price: String,
    val status: String,
    val occupancy: Float, // 0.0 to 1.0
    val comment: String = "",
    val color: Color = Color.Gray
)
