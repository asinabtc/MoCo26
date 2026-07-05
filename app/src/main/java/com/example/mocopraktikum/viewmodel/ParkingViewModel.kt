package com.example.mocopraktikum.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.model.ParkingSpotWithReviews
import com.example.mocopraktikum.model.Review
import com.example.mocopraktikum.repository.ParkingRepository
import com.example.mocopraktikum.data.UserPreferencesRepository
import com.example.mocopraktikum.receiver.ParkingEventReceiver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ParkingViewModel(
    private val application: Application,
    private val repository: ParkingRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // --- DataStore / Settings ---
    val userName: StateFlow<String> = userPreferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Gast")

    val isDarkMode: StateFlow<Boolean> = userPreferencesRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateUserName(name: String) {
        viewModelScope.launch { userPreferencesRepository.saveUserName(name) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setDarkMode(enabled) }
    }

    // --- Room / Parking Spots ---

    // Alle Parkplätze inklusive ihrer Reviews als Flow
    val spotsWithReviews: StateFlow<List<ParkingSpotWithReviews>> = repository.allSpotsWithReviews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Legacy Support für die einfache Liste (falls noch in der UI genutzt)
    val parkingSpots: StateFlow<List<ParkingSpot>> = repository.allParkingSpots
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedSpot = MutableStateFlow<ParkingSpot?>(null)
    val selectedSpot: StateFlow<ParkingSpot?> = _selectedSpot.asStateFlow()

    // Kombiniert den ausgewählten Spot mit den aktuellen Review-Daten
    val selectedSpotWithReviews: StateFlow<ParkingSpotWithReviews?> = combine(
        _selectedSpot,
        spotsWithReviews
    ) { selected, allWithReviews ->
        allWithReviews.find { it.parkingSpot.id == selected?.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectSpot(spot: ParkingSpot) {
        _selectedSpot.value = spot
        viewModelScope.launch {
            userPreferencesRepository.saveLastViewedSpotId(spot.id)
        }
    }

    fun addParkingSpot(
        title: String,
        street: String,
        zipCode: String,
        city: String,
        isPaid: Boolean,
        comment: String,
        status: String = "frei",
        latitude: Double? = null,
        longitude: Double? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val occupancy = when (status) {
                "frei" -> 0.0f
                "mäßig besucht" -> 0.5f
                "voll" -> 1.0f
                else -> 0.0f
            }
            val color = when (status) {
                "frei" -> Color(0xFF4CAF50)
                "mäßig besucht" -> Color(0xFFFF9800)
                "voll" -> Color(0xFFF44336)
                else -> Color(0xFF4CAF50)
            }

            val newSpot = ParkingSpot(
                id = System.currentTimeMillis().toString(),
                title = title,
                street = street,
                zipCode = zipCode,
                city = city,
                latitude = latitude,
                longitude = longitude,
                distance = "0 m",
                price = if (isPaid) "kostenpflichtig" else "kostenlos",
                status = status,
                occupancy = occupancy,
                comment = comment,
                color = color
            )
            repository.saveParkingSpot(newSpot)
            _isLoading.value = false
            onComplete()

            // Broadcast senden
            val intent = Intent(application, ParkingEventReceiver::class.java).apply {
                action = ParkingEventReceiver.ACTION_SPOT_ADDED
                putExtra(ParkingEventReceiver.EXTRA_TITLE, "Neuer Parkplatz")
                putExtra(ParkingEventReceiver.EXTRA_MESSAGE, "Ein neuer Parkplatz wurde in deiner Stadt eingefügt: $title")
            }
            application.sendBroadcast(intent)
        }
    }

    fun addReview(spotId: String, rating: Int, text: String) {
        viewModelScope.launch {
            val review = Review(
                spotId = spotId,
                rating = rating,
                text = text
            )
            repository.addReview(review)
        }
    }
    
    fun updateOccupancy(id: String, occupancy: Float) {
        viewModelScope.launch {
            val spot = parkingSpots.value.find { it.id == id } ?: return@launch
            val oldStatus = spot.status
            
            val newStatus = when {
                occupancy < 0.3f -> "frei"
                occupancy < 0.8f -> "mäßig besucht"
                else -> "voll"
            }
            val newColor = when {
                occupancy < 0.3f -> Color(0xFF4CAF50)
                occupancy < 0.8f -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
            
            val updatedSpot = spot.copy(
                occupancy = occupancy,
                status = newStatus,
                color = newColor
            )
            repository.updateParkingSpot(updatedSpot)
            
            // Check for last viewed and status change to "frei"
            val lastViewedId = userPreferencesRepository.lastViewedSpotId.first()
            if (id == lastViewedId && oldStatus != "frei" && newStatus == "frei") {
                val intent = Intent(application, ParkingEventReceiver::class.java).apply {
                    action = ParkingEventReceiver.ACTION_STATUS_CHANGED
                    putExtra(ParkingEventReceiver.EXTRA_TITLE, "Parkplatz jetzt frei!")
                    putExtra(ParkingEventReceiver.EXTRA_MESSAGE, "Der Status von '${spot.title}' ist auf frei gewechselt.")
                }
                application.sendBroadcast(intent)
            }

            if (_selectedSpot.value?.id == id) {
                _selectedSpot.value = updatedSpot
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.example.mocopraktikum.ParkingApplication
                return ParkingViewModel(
                    application,
                    application.repository,
                    application.userPreferencesRepository
                ) as T
            }
        }
    }
}
