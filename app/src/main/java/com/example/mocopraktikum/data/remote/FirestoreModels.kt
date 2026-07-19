package com.example.mocopraktikum.data.remote

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.model.Review

/**
 * Firestore-Datenklassen (DTOs).
 *
 * Firestore kann die Domänen-Modelle nicht direkt speichern:
 *  - [ParkingSpot] enthält ein Compose-[Color]-Feld, das nicht serialisierbar ist.
 *  - Firestore braucht einen argumentlosen Konstruktor -> alle Felder haben Default-Werte.
 *
 * Deshalb wird beim Hoch-/Runterladen zwischen Domänen-Modell und DTO gemappt.
 * Hinweis: Das Boolean-Feld heißt bewusst "managed" (nicht "isManaged"), weil Firestore
 * bei Kotlin-Booleans mit "is"-Präfix den Feldnamen sonst kürzen würde.
 */
data class ParkingSpotDto(
    val id: String = "",
    val title: String = "",
    val street: String = "",
    val zipCode: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distance: String = "",
    val price: String = "",
    val status: String = "",
    val occupancy: Double = 0.0,
    val comment: String = "",
    val colorArgb: Long = 0L,
    val managed: Boolean = false
)

data class ReviewDto(
    val id: String = "",
    val spotId: String = "",
    val rating: Int = 0,
    val text: String = "",
    val timestamp: Long = 0L
)

// --- Mapping: Domäne -> DTO ---

fun ParkingSpot.toDto(): ParkingSpotDto = ParkingSpotDto(
    id = id,
    title = title,
    street = street,
    zipCode = zipCode,
    city = city,
    latitude = latitude,
    longitude = longitude,
    distance = distance,
    price = price,
    status = status,
    occupancy = occupancy.toDouble(),
    comment = comment,
    colorArgb = color.toArgb().toLong(),
    managed = isManaged
)

fun Review.toDto(): ReviewDto = ReviewDto(
    id = id,
    spotId = spotId,
    rating = rating,
    text = text,
    timestamp = timestamp
)

// --- Mapping: DTO -> Domäne ---

fun ParkingSpotDto.toDomain(): ParkingSpot = ParkingSpot(
    id = id,
    title = title,
    street = street,
    zipCode = zipCode,
    city = city,
    latitude = latitude,
    longitude = longitude,
    distance = distance,
    price = price,
    status = status,
    occupancy = occupancy.toFloat(),
    comment = comment,
    color = Color(colorArgb.toInt()),
    isManaged = managed
)

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    spotId = spotId,
    rating = rating,
    text = text,
    timestamp = timestamp
)
