package com.example.mocopraktikum.repository

import com.example.mocopraktikum.data.ParkingSpotDao
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.model.ParkingSpotWithReviews
import com.example.mocopraktikum.model.Review
import kotlinx.coroutines.flow.Flow

class ParkingRepository(private val parkingSpotDao: ParkingSpotDao) {

    val allParkingSpots: Flow<List<ParkingSpot>> = parkingSpotDao.getAllSpots()
    
    val allSpotsWithReviews: Flow<List<ParkingSpotWithReviews>> = parkingSpotDao.getSpotsWithReviews()

    suspend fun saveParkingSpot(spot: ParkingSpot) {
        parkingSpotDao.insertSpot(spot)
    }

    suspend fun updateParkingSpot(spot: ParkingSpot) {
        parkingSpotDao.updateSpot(spot)
    }

    suspend fun deleteParkingSpot(spot: ParkingSpot) {
        parkingSpotDao.deleteSpot(spot)
    }

    suspend fun addReview(review: Review) {
        parkingSpotDao.insertReview(review)
    }
}
