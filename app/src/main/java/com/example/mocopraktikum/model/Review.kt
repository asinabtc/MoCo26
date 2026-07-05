package com.example.mocopraktikum.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = ParkingSpot::class,
            parentColumns = ["id"],
            childColumns = ["spotId"],
            onDelete = ForeignKey.CASCADE // Wenn der Parkplatz gelöscht wird, werden auch die Reviews gelöscht
        )
    ],
    indices = [Index("spotId")]
)
data class Review(
    @PrimaryKey(autoGenerate = true)
    val reviewId: Long = 0,
    val spotId: String, // Verknüpfung zum ParkingSpot
    val rating: Int,    // 1-5 Sterne
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
