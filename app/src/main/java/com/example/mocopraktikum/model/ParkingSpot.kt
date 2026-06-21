package com.example.mocopraktikum.model

import androidx.compose.ui.graphics.Color

data class ParkingSpot(
    val id: String,
    val title: String,
    val distance: String,
    val price: String,
    val status: String,
    val occupancy: Float, // 0.0 to 1.0
    val comment: String = "",
    val color: Color = Color.Gray
)
