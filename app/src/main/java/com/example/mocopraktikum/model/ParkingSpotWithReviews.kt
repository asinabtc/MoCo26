package com.example.mocopraktikum.model

import androidx.room.Embedded
import androidx.room.Relation

data class ParkingSpotWithReviews(
    @Embedded val parkingSpot: ParkingSpot,
    @Relation(
        parentColumn = "id",
        entityColumn = "spotId"
    )
    val reviews: List<Review>
)
