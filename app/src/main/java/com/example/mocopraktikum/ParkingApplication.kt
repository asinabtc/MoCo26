package com.example.mocopraktikum

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.mocopraktikum.data.AppDatabase
import com.example.mocopraktikum.data.UserPreferencesRepository
import com.example.mocopraktikum.data.remote.ParkingRemoteDataSource
import com.example.mocopraktikum.repository.ParkingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.osmdroid.config.Configuration

class ParkingApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ParkingRepository(database.parkingSpotDao(), ParkingRemoteDataSource()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    // App-weiter Scope für die dauerhafte Firestore-Synchronisation
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize OSMDroid
        // Eindeutiger User-Agent (NICHT "com.example.*", sonst blockt OpenStreetMap mit HTTP 403)
        Configuration.getInstance().userAgentValue = "ParkSpotter/1.0 (meryemselo13@gmail.com)"

        // Startet die Synchronisation der geteilten Daten (Parkplätze + Bewertungen) mit Firestore
        repository.startSync(applicationScope)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ParkSpotter Notifications"
            val descriptionText = "Benachrichtigungen für neue Parkplätze"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("parking_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
