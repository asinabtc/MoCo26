package com.example.mocopraktikum.data

import androidx.room.*
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.model.ParkingSpotWithReviews
import com.example.mocopraktikum.model.Review
import kotlinx.coroutines.flow.Flow

@Dao
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

    @Insert
    suspend fun insertReview(review: Review)
}
