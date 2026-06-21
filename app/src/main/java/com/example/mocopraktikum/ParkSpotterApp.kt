package com.example.mocopraktikum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mocopraktikum.screens.AddParkingSpotScreen
import com.example.mocopraktikum.screens.HomeScreen
import com.example.mocopraktikum.screens.ParkingDetailsScreen
import com.example.mocopraktikum.viewmodel.ParkingViewModel

@Composable
fun ParkSpotterApp() {
    val navController = rememberNavController()
    val viewModel: ParkingViewModel = viewModel()
    
    val spots by viewModel.parkingSpots.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSpot by viewModel.selectedSpot.collectAsState()

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
                }
            )
        }

        composable("add") {
            AddParkingSpotScreen(
                onBackClick = { navController.popBackStack() },
                onConfirmClick = { location, spotsCount, comment, isPaid ->
                    viewModel.addParkingSpot(location, spotsCount, comment, isPaid)
                }
            )
        }

        composable("details") {
            ParkingDetailsScreen(
                spot = selectedSpot,
                onBackClick = { navController.popBackStack() },
                onUpdateOccupancy = { id, occupancy ->
                    viewModel.updateOccupancy(id, occupancy)
                }
            )
        }
    }
}
