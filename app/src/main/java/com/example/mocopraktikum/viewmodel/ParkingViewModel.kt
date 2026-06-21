package com.example.mocopraktikum.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ParkingViewModel : ViewModel() {

    private val repository = ParkingRepository()

    private val _parkingSpots = MutableStateFlow<List<ParkingSpot>>(emptyList())
    val parkingSpots: StateFlow<List<ParkingSpot>> = _parkingSpots.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedSpot = MutableStateFlow<ParkingSpot?>(null)
    val selectedSpot: StateFlow<ParkingSpot?> = _selectedSpot.asStateFlow()

    init {
        loadSpots()
        startLiveUpdates()
    }

    private fun startLiveUpdates() {
        viewModelScope.launch {
            while (isActive) {
                delay(30000) // 30 Sekunden warten
                refreshSpotsSilently()
            }
        }
    }

    private suspend fun refreshSpotsSilently() {
        val updatedSpots = repository.getParkingSpots()
        _parkingSpots.value = updatedSpots
        
        // Auch den aktuell ausgewählten Spot aktualisieren, falls vorhanden
        _selectedSpot.value?.let { current ->
            _selectedSpot.value = updatedSpots.find { it.id == current.id }
        }
    }

    fun loadSpots() {
        viewModelScope.launch {
            _isLoading.value = true
            _parkingSpots.value = repository.getParkingSpots()
            _isLoading.value = false
        }
    }

    fun selectSpot(spot: ParkingSpot) {
        _selectedSpot.value = spot
    }

    fun addParkingSpot(title: String, spotsCount: String, comment: String, isPaid: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val newSpot = ParkingSpot(
                id = (System.currentTimeMillis()).toString(),
                title = title,
                distance = "0 m",
                price = if (isPaid) "kostenpflichtig" else "kostenlos",
                status = "frei",
                occupancy = 0.0f,
                comment = comment,
                color = Color(0xFF4CAF50)
            )
            repository.saveParkingSpot(newSpot)
            loadSpots() // Liste neu laden
        }
    }
    
    fun updateOccupancy(id: String, occupancy: Float) {
        // Hier könnte man auch einen Repository-Update-Call machen
        _parkingSpots.value = _parkingSpots.value.map {
            if (it.id == id) {
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
                it.copy(occupancy = occupancy, status = newStatus, color = newColor)
            } else it
        }
        if (_selectedSpot.value?.id == id) {
            _selectedSpot.value = _parkingSpots.value.find { it.id == id }
        }
    }
}
