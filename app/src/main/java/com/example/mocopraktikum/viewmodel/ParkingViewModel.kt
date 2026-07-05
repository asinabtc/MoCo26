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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.URL

class ParkingViewModel(
    private val application: Application,
    private val repository: ParkingRepository,
    val userPreferencesRepository: UserPreferencesRepository
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

    val spotsWithReviews: StateFlow<List<ParkingSpotWithReviews>> = repository.allSpotsWithReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parkingSpots: StateFlow<List<ParkingSpot>> = repository.allParkingSpots
        .onEach { list ->
            // Reparatur-Check: Falls ein lokaler Spot keine Koordinaten hat, versuchen wir sie zu finden
            list.forEach { spot ->
                if (spot.latitude == null || spot.longitude == null) {
                    fixMissingCoordinates(spot)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun fixMissingCoordinates(spot: ParkingSpot) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(application)
                
                // Versuch 1: Volle Adresse
                val fullAddress = "${spot.street}, ${spot.zipCode} ${spot.city}"
                @Suppress("DEPRECATION")
                var addresses = geocoder.getFromLocationName(fullAddress, 1)
                
                // Versuch 2: Titel + Stadt (falls Straße fehlt oder nicht gefunden wurde)
                if (addresses.isNullOrEmpty() && spot.title.isNotBlank()) {
                    val titleCity = "${spot.title}, ${spot.city}"
                    @Suppress("DEPRECATION")
                    addresses = geocoder.getFromLocationName(titleCity, 1)
                }
                
                if (addresses?.isNotEmpty() == true) {
                    val updatedSpot = spot.copy(
                        latitude = addresses[0].latitude,
                        longitude = addresses[0].longitude
                    )
                    repository.updateParkingSpot(updatedSpot)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedSpot = MutableStateFlow<ParkingSpot?>(null)
    val selectedSpot: StateFlow<ParkingSpot?> = _selectedSpot.asStateFlow()

    val selectedSpotWithReviews: StateFlow<ParkingSpotWithReviews?> = combine(
        _selectedSpot,
        spotsWithReviews
    ) { selected, allWithReviews ->
        if (selected == null) return@combine null
        val fromDb = allWithReviews.find { it.parkingSpot.id == selected.id }
        fromDb ?: ParkingSpotWithReviews(parkingSpot = selected, reviews = emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _mapCenterEvent = MutableSharedFlow<GeoPoint>()
    val mapCenterEvent = _mapCenterEvent.asSharedFlow()

    private val _currentUserLocation = MutableStateFlow<android.location.Location?>(null)
    val currentUserLocation = _currentUserLocation.asStateFlow()

    fun updateLocation(location: android.location.Location) {
        _currentUserLocation.value = location
    }

    val spotsWithDistance: StateFlow<List<ParkingSpot>> = combine(
        parkingSpots,
        _currentUserLocation
    ) { spots, location ->
        if (location == null) return@combine spots
        
        spots.map { spot ->
            if (spot.latitude == null || spot.longitude == null) return@map spot
            
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                location.latitude, location.longitude,
                spot.latitude, spot.longitude,
                results
            )
            val dist = results[0]
            val distStr = if (dist < 1000) {
                "${dist.toInt()} m"
            } else {
                "%.1f km".format(dist / 1000f)
            }
            spot.copy(distance = distStr)
        }.sortedBy { spot ->
            // Optional: Sortiere nach Entfernung
            val results = FloatArray(1)
            if (spot.latitude != null && spot.longitude != null) {
                android.location.Location.distanceBetween(
                    location.latitude, location.longitude,
                    spot.latitude, spot.longitude,
                    results
                )
                results[0]
            } else Float.MAX_VALUE
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSpot(spot: ParkingSpot) {
        _selectedSpot.value = spot
        viewModelScope.launch {
            userPreferencesRepository.saveLastViewedSpotId(spot.id)
        }
    }

    fun addParkingSpot(
        title: String, street: String, zipCode: String, city: String,
        isPaid: Boolean, comment: String, status: String = "frei",
        latitude: Double? = null, longitude: Double? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            var finalLat = latitude
            var finalLon = longitude

            // Falls keine Koordinaten vorhanden sind, versuchen wir die Adresse zu geocoden
            if (finalLat == null || finalLon == null) {
                try {
                    val geocoder = android.location.Geocoder(application)
                    // Versuch 1: Volle Adresse
                    val fullAddress = "$street, $zipCode $city"
                    @Suppress("DEPRECATION")
                    var addresses = geocoder.getFromLocationName(fullAddress, 1)
                    
                    // Versuch 2: Nur Titel und Stadt
                    if (addresses.isNullOrEmpty()) {
                        @Suppress("DEPRECATION")
                        addresses = geocoder.getFromLocationName("$title, $city", 1)
                    }

                    if (addresses?.isNotEmpty() == true) {
                        finalLat = addresses[0].latitude
                        finalLon = addresses[0].longitude
                    } else {
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(application, "Koordinaten für '$title' nicht gefunden. Erscheint nur in der Liste.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }

            val color = when (status) {
                "frei" -> Color(0xFF4CAF50)
                "mäßig besucht" -> Color(0xFFFF9800)
                "voll" -> Color(0xFFF44336)
                else -> Color(0xFF4CAF50)
            }

            val newSpot = ParkingSpot(
                id = System.currentTimeMillis().toString(),
                title = title, street = street, zipCode = zipCode, city = city,
                latitude = finalLat, longitude = finalLon,
                distance = "0 m", price = if (isPaid) "kostenpflichtig" else "kostenlos",
                status = status, occupancy = if (status == "frei") 0f else if (status == "voll") 1f else 0.5f,
                comment = comment, color = color,
                isManaged = true
            )
            repository.saveParkingSpot(newSpot)
            _isLoading.value = false
            onComplete()
        }
    }

    fun searchLocation(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val geocoder = android.location.Geocoder(application)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                if (addresses?.isNotEmpty() == true) {
                    val addr = addresses[0]
                    val point = GeoPoint(addr.latitude, addr.longitude)
                    _mapCenterEvent.emit(point)
                    fetchOSMParkingSpots(addr.latitude, addr.longitude)
                }
            } catch (e: Exception) { e.printStackTrace() }
            _isLoading.value = false
        }
    }

    fun searchInArea(lat: Double, lon: Double) {
        viewModelScope.launch {
            userPreferencesRepository.saveLastLocation(lat, lon)
            fetchOSMParkingSpots(lat, lon)
        }
    }

    fun fetchOSMParkingSpots(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val overpassUrl = "https://overpass-api.de/api/interpreter?data=[out:json];(node[\"amenity\"=\"parking\"](around:1500,$lat,$lon);way[\"amenity\"=\"parking\"](around:1500,$lat,$lon););out center;"
                val result = withContext(Dispatchers.IO) { URL(overpassUrl).readText() }
                val json = JSONObject(result)
                val elements = json.getJSONArray("elements")
                val spots = mutableListOf<ParkingSpot>()
                
                for (i in 0 until elements.length()) {
                    val el = elements.getJSONObject(i)
                    val pLat = el.optDouble("lat", el.optJSONObject("center")?.optDouble("lat") ?: 0.0)
                    val pLon = el.optDouble("lon", el.optJSONObject("center")?.optDouble("lon") ?: 0.0)
                    val tags = el.optJSONObject("tags")
                    val name = tags?.optString("name") ?: "Öffentlicher Parkplatz"
                    
                    val street = tags?.optString("addr:street")
                    val houseNumber = tags?.optString("addr:housenumber") ?: ""
                    val fullStreet = if (street != null) "$street $houseNumber".trim() else ""
                    
                    val city = tags?.optString("addr:city") ?: ""
                    val zip = tags?.optString("addr:postcode") ?: ""
                    
                    val fee = tags?.optString("fee")
                    val feeInfo = when(fee) {
                        "yes" -> "kostenpflichtig"
                        "no" -> "kostenlos"
                        else -> "Info s. OSM"
                    }
                    
                    val capacity = tags?.optString("capacity")
                    val access = tags?.optString("access")
                    
                    var comment = ""
                    if (!capacity.isNullOrBlank()) comment += "Kapazität: $capacity. "
                    if (!access.isNullOrBlank()) comment += "Zugang: $access."

                    spots.add(ParkingSpot(
                        id = "osm_${el.getLong("id")}",
                        title = name,
                        street = fullStreet,
                        zipCode = zip,
                        city = city,
                        latitude = pLat, longitude = pLon,
                        distance = "Umkreis", price = feeInfo,
                        status = "öffentlich", occupancy = 0f,
                        comment = comment,
                        color = Color(0xFF2196F3),
                        isManaged = false
                    ))
                }
                spots.forEach { repository.saveParkingSpot(it) }
                resolveMissingAddresses(spots)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun resolveMissingAddresses(spots: List<ParkingSpot>) {
        viewModelScope.launch(Dispatchers.IO) {
            val geocoder = android.location.Geocoder(application)
            
            for (spot in spots) {
                // Nur wenn Straße fehlt UND wir Koordinaten haben
                if (spot.street.isBlank() && spot.latitude != null && spot.longitude != null) {
                    try {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(spot.latitude, spot.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val resolvedStreet = "${addr.thoroughfare ?: ""} ${addr.subThoroughfare ?: ""}".trim()
                            val resolvedCity = addr.locality ?: ""
                            val resolvedZip = addr.postalCode ?: ""
                            
                            val updatedSpot = spot.copy(
                                street = resolvedStreet.ifBlank { "Unbekannte Straße" },
                                city = resolvedCity.ifBlank { spot.city },
                                zipCode = resolvedZip.ifBlank { spot.zipCode }
                            )
                            repository.updateParkingSpot(updatedSpot)
                        }
                    } catch (e: Exception) { }
                }
            }
        }
    }

    fun updateOccupancy(id: String, occupancy: Float) {
        viewModelScope.launch {
            val spot = parkingSpots.value.find { it.id == id } ?: return@launch
            
            val newStatus = when {
                occupancy < 0.3f -> "frei"
                occupancy < 0.8f -> "mäßig besucht"
                else -> "voll"
            }
            
            val newColor = when (newStatus) {
                "frei" -> Color(0xFF4CAF50) // Green
                "mäßig besucht" -> Color(0xFFFF9800) // Orange
                "voll" -> Color(0xFFF44336) // Red
                else -> Color(0xFF4CAF50)
            }

            val updatedSpot = spot.copy(
                occupancy = occupancy, 
                status = newStatus,
                color = newColor,
                isManaged = true
            )
            repository.updateParkingSpot(updatedSpot)
        }
    }

    fun addReview(spotId: String, rating: Int, text: String) {
        viewModelScope.launch {
            repository.addReview(Review(spotId = spotId, rating = rating, text = text))
        }
    }

    fun deleteParkingSpot(spot: ParkingSpot) {
        viewModelScope.launch {
            repository.deleteParkingSpot(spot)
            // Falls der gelöschte Spot der aktuell ausgewählte war, Selektion aufheben
            if (_selectedSpot.value?.id == spot.id) {
                _selectedSpot.value = null
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as com.example.mocopraktikum.ParkingApplication
                return ParkingViewModel(app, app.repository, app.userPreferencesRepository) as T
            }
        }
    }
}
