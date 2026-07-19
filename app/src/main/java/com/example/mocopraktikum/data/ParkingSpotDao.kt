package com.example.mocopraktikum.data

import androidx.room.*
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.model.ParkingSpotWithReviews
import com.example.mocopraktikum.model.Review
import kotlinx.coroutines.flow.Flow

@Dao   //Data Access Object, also spricht direkt mit Room Datenbank (enthält alle Datenabfragen z.B. lesen, löschen, ändern, speichern)
interface ParkingSpotDao {
    @Query("SELECT * FROM parking_spots")
    fun getAllSpots(): Flow<List<ParkingSpot>>

    @Query("SELECT * FROM parking_spots WHERE id = :id")
    suspend fun getSpotById(id: String): ParkingSpot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: ParkingSpot)

    @Update
    suspend fun updateSpot(spot: ParkingSpot)

    @Delete
    suspend fun deleteSpot(spot: ParkingSpot)

    // --- Relations ---

    @Transaction
    @Query("SELECT * FROM parking_spots")
    fun getSpotsWithReviews(): Flow<List<ParkingSpotWithReviews>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    // --- Sync (Spiegelung der Firestore-Daten in die lokale Room-DB) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpots(spots: List<ParkingSpot>)

    /** Entfernt lokal gespiegelte, geteilte Parkplätze, die es in Firestore nicht mehr gibt.
     *  OSM-/rein lokale Spots (isManaged = 0) bleiben unangetastet. */
    @Query("DELETE FROM parking_spots WHERE isManaged = 1 AND id NOT IN (:ids)")
    suspend fun deleteManagedNotIn(ids: List<String>)

    @Query("DELETE FROM parking_spots WHERE isManaged = 1")
    suspend fun deleteAllManaged()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Query("DELETE FROM reviews WHERE id NOT IN (:ids)")
    suspend fun deleteReviewsNotIn(ids: List<String>)

    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()
}
