package com.example.mocopraktikum

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
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

@Composable
fun ParkSpotterApp() {
    val navController = rememberNavController()
    val viewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory)
    
    // Permission Request for Notifications (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Hier könnte man auf das Ergebnis reagieren
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val spots by viewModel.parkingSpots.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSpotWithReviews by viewModel.selectedSpotWithReviews.collectAsState()
    
    // DataStore states
    val userName by viewModel.userName.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    MoCoPraktikumTheme(darkTheme = isDarkMode) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    spots = spots,
                    isLoading = isLoading,
                    onAddClick = { navController.navigate("add") },
                    onSpotClick = { spot -> 
                        viewModel.selectSpot(spot)
                        navController.navigate("details") 
                    },
                    onProfileClick = { navController.navigate("settings") }
                )
            }

            composable("add") {
                AddParkingSpotScreen(
                    onBackClick = { navController.popBackStack() },
                    onConfirmClick = { title, street, zipCode, city, isPaid, comment, status ->
                        viewModel.addParkingSpot(title, street, zipCode, city, isPaid, comment, status) {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable("details") {
                ParkingDetailsScreen(
                    spotWithReviews = selectedSpotWithReviews,
                    onBackClick = { navController.popBackStack() },
                    onUpdateOccupancy = { id, occupancy ->
                        viewModel.updateOccupancy(id, occupancy)
                    },
                    onAddReview = { rating, text ->
                        selectedSpotWithReviews?.parkingSpot?.id?.let { id ->
                            viewModel.addReview(id, rating, text)
                        }
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
