package com.example.mocopraktikum.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "reviews",
    // Kein ForeignKey mehr: Reviews werden zwischen Geräten synchronisiert und können
    // auch für Parkplätze ankommen, die lokal (noch) nicht vorhanden sind (z.B. OSM-Spots,
    // die das andere Gerät noch nicht geladen hat). Der Index bleibt für schnelle Abfragen.
    indices = [Index("spotId")]
)
data class Review(
    // Stabile String-ID (= Firestore-Dokument-ID), damit die Synchronisation idempotent ist
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val spotId: String = "", // Verknüpfung zum ParkingSpot
    val rating: Int = 0,     // 1-5 Sterne
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
