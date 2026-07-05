package com.example.mocopraktikum

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mocopraktikum.screens.*
import com.example.mocopraktikum.viewmodel.ParkingViewModel
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun ParkSpotterApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val viewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory)
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> 
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            // Re-trigger location updates if needed
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Standort-Updates an das ViewModel liefern
    DisposableEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()
            
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { viewModel.updateLocation(it) }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper())
        } catch (e: SecurityException) { }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    val spots by viewModel.spotsWithDistance.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSpotWithReviews by viewModel.selectedSpotWithReviews.collectAsState()
    
    val userName by viewModel.userName.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    MoCoPraktikumTheme(darkTheme = isDarkMode) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    spots = spots,
                    isLoading = isLoading,
                    mapCenterEvent = viewModel.mapCenterEvent,
                    onSearch = { viewModel.searchLocation(it) },
                    onSearchInArea = { lat, lon -> viewModel.searchInArea(lat, lon) },
                    onAddClick = { navController.navigate("add") },
                    onSpotClick = { spot -> 
                        viewModel.selectSpot(spot)
                        navController.navigate("details") 
                    },
                    onProfileClick = { navController.navigate("settings") },
                    userPreferencesRepository = viewModel.userPreferencesRepository
                )
            }

            composable("add") {
                AddParkingSpotScreen(
                    onBackClick = { navController.popBackStack() },
                    onConfirmClick = { title, street, zipCode, city, isPaid, comment, status, lat, lng ->
                        viewModel.addParkingSpot(title, street, zipCode, city, isPaid, comment, status, lat, lng) {
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        }
                    }
                )
            }

            composable("details") {
                ParkingDetailsScreen(
                    spotWithReviews = selectedSpotWithReviews,
                    onBackClick = { navController.popBackStack() },
                    onUpdateOccupancy = { id, occupancy -> viewModel.updateOccupancy(id, occupancy) },
                    onAddReview = { rating, text ->
                        selectedSpotWithReviews?.parkingSpot?.id?.let { id -> viewModel.addReview(id, rating, text) }
                    },
                    onDeleteSpot = { spot ->
                        viewModel.deleteParkingSpot(spot)
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    currentName = userName,
                    isDarkMode = isDarkMode,
                    onBackClick = { navController.popBackStack() },
                    onNameChange = { viewModel.updateUserName(it) },
                    onDarkModeToggle = { viewModel.toggleDarkMode(it) }
                )
            }
        }
    }
}
