package com.example.mocopraktikum

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.mocopraktikum.data.AppDatabase
import com.example.mocopraktikum.data.UserPreferencesRepository
import com.example.mocopraktikum.repository.ParkingRepository

class ParkingApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ParkingRepository(database.parkingSpotDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
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
