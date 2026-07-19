package com.example.mocopraktikum.data.remote

import android.util.Log
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Kapselt den kompletten Firestore-Zugriff.
 *
 * Zwei Collections:
 *  - "parking_spots": von Nutzer*innen angelegte/aktualisierte Parkplätze
 *  - "reviews":       Bewertungen zu Parkplätzen
 *
 * Die *Flow-Methoden liefern per SnapshotListener Echtzeit-Updates. Dank aktivierter
 * Offline-Persistenz (Firestore-Standard auf Android) feuern die Listener auch sofort
 * nach lokalen Schreibvorgängen und funktionieren offline.
 */
class ParkingRemoteDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val spotsCollection = db.collection("parking_spots")
    private val reviewsCollection = db.collection("reviews")

    fun parkingSpotsFlow(): Flow<List<ParkingSpot>> = callbackFlow {
        val registration = spotsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Fehler beim Laden der Parkplätze", error)
                return@addSnapshotListener
            }
            val spots = snapshot?.documents
                ?.mapNotNull { it.toObject(ParkingSpotDto::class.java)?.toDomain() }
                ?: emptyList()
            trySend(spots)
        }
        awaitClose { registration.remove() }
    }

    fun reviewsFlow(): Flow<List<Review>> = callbackFlow {
        val registration = reviewsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Fehler beim Laden der Bewertungen", error)
                return@addSnapshotListener
            }
            val reviews = snapshot?.documents
                ?.mapNotNull { it.toObject(ReviewDto::class.java)?.toDomain() }
                ?: emptyList()
            trySend(reviews)
        }
        awaitClose { registration.remove() }
    }

    // Schreibvorgänge bewusst ohne await(): Firestore schreibt sofort in den lokalen Cache
    // (Offline-Persistenz), der SnapshotListener aktualisiert die UI unmittelbar und die
    // Übertragung zum Server passiert im Hintergrund. So blockiert nichts – auch offline.
    fun uploadSpot(spot: ParkingSpot) {
        spotsCollection.document(spot.id).set(spot.toDto())
    }

    fun deleteSpot(spotId: String) {
        spotsCollection.document(spotId).delete()
    }

    fun uploadReview(review: Review) {
        reviewsCollection.document(review.id).set(review.toDto())
    }

    /** Löscht alle Bewertungen eines Parkplatzes (z.B. wenn der Parkplatz entfernt wird). */
    suspend fun deleteReviewsForSpot(spotId: String) {
        val docs = reviewsCollection.whereEqualTo("spotId", spotId).get().await()
        for (doc in docs.documents) {
            doc.reference.delete()
        }
    }

    companion object {
        private const val TAG = "ParkingRemoteDataSource"
    }
}
