package com.example.mocopraktikum.repository

import com.example.mocopraktikum.data.ParkingSpotDao
import com.example.mocopraktikum.data.remote.ParkingRemoteDataSource
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.model.ParkingSpotWithReviews
import com.example.mocopraktikum.model.Review
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Vermittelt zwischen lokaler Room-DB und der geteilten Firestore-Datenbank.
 *
 * Konzept:
 *  - Die UI liest weiterhin ausschließlich aus Room (unveränderte Flows unten).
 *  - Geteilte Daten (von Nutzer*innen angelegte Parkplätze [isManaged] + alle Bewertungen)
 *    leben in Firestore. [startSync] abonniert Firestore und spiegelt alles nach Room,
 *    wodurch die UI automatisch aktualisiert wird.
 *  - Rein lokale/öffentliche OSM-Parkplätze (isManaged = false) bleiben nur in Room und
 *    landen nicht in Firestore (spart Speicher & Freikontingent).
 */
class ParkingRepository(
    private val parkingSpotDao: ParkingSpotDao,
    private val remote: ParkingRemoteDataSource
) {

    val allParkingSpots: Flow<List<ParkingSpot>> = parkingSpotDao.getAllSpots()

    val allSpotsWithReviews: Flow<List<ParkingSpotWithReviews>> = parkingSpotDao.getSpotsWithReviews()

    /** Startet die kontinuierliche Synchronisation Firestore -> Room. */
    fun startSync(scope: CoroutineScope) {
        scope.launch {
            remote.parkingSpotsFlow().collect { spots ->
                parkingSpotDao.insertSpots(spots)
                if (spots.isEmpty()) {
                    parkingSpotDao.deleteAllManaged()
                } else {
                    parkingSpotDao.deleteManagedNotIn(spots.map { it.id })
                }
            }
        }
        scope.launch {
            remote.reviewsFlow().collect { reviews ->
                parkingSpotDao.insertReviews(reviews)
                if (reviews.isEmpty()) {
                    parkingSpotDao.deleteAllReviews()
                } else {
                    parkingSpotDao.deleteReviewsNotIn(reviews.map { it.id })
                }
            }
        }
    }

    suspend fun saveParkingSpot(spot: ParkingSpot) {
        if (spot.isManaged) {
            // Geteilter Parkplatz -> Firestore; der Sync-Listener spiegelt ihn nach Room.
            remote.uploadSpot(spot)
        } else {
            // Öffentlicher OSM-/lokaler Spot -> nur lokal.
            parkingSpotDao.insertSpot(spot)
        }
    }

    suspend fun updateParkingSpot(spot: ParkingSpot) {
        if (spot.isManaged) {
            remote.uploadSpot(spot)
        } else {
            parkingSpotDao.updateSpot(spot)
        }
    }

    suspend fun deleteParkingSpot(spot: ParkingSpot) {
        if (spot.isManaged) {
            remote.deleteSpot(spot.id)
            remote.deleteReviewsForSpot(spot.id)
        } else {
            parkingSpotDao.deleteSpot(spot)
        }
    }

    suspend fun addReview(review: Review) {
        // Bewertungen werden immer geteilt -> Firestore; Sync-Listener spiegelt nach Room.
        remote.uploadReview(review)
    }
}
