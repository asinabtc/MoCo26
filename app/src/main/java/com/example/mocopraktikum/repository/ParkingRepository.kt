package com.example.mocopraktikum.repository

import androidx.compose.ui.graphics.Color
import com.example.mocopraktikum.model.ParkingSpot
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParkingRepository {

    // Simuliert eine Datenbank oder API
    private var mockDatabase = mutableListOf(
        ParkingSpot("1", "Bahnhofstraße", "200 m", "kostenlos", "frei", 0.2f, color = Color(0xFF4CAF50)),
        ParkingSpot("2", "Innenstadt", "450 m", "kostenpflichtig", "mäßig besucht", 0.6f, color = Color(0xFFFF9800)),
        ParkingSpot("3", "Supermarkt", "700 m", "kostenlos", "voll", 0.95f, color = Color(0xFFF44336))
    )

    suspend fun getParkingSpots(): List<ParkingSpot> = withContext(Dispatchers.IO) {
        delay(1500) // Simuliere Netzwerk-Verzögerung von 1,5 Sekunden
        return@withContext mockDatabase.toList()
    }

    suspend fun saveParkingSpot(spot: ParkingSpot) = withContext(Dispatchers.IO) {
        delay(1000) // Simuliere Speicher-Verzögerung
        mockDatabase.add(spot)
    }
}
