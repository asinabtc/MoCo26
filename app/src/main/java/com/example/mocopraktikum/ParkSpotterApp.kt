package com.example.mocopraktikum


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mocopraktikum.screens.AddParkingSpotScreen
import com.example.mocopraktikum.screens.HomeScreen
import com.example.mocopraktikum.screens.ParkingDetailsScreen

@Composable
fun ParkSpotterApp() {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeScreen(
            onAddClick = { currentScreen = "add" },
            onSpotClick = { currentScreen = "details" }
        )

        "add" -> AddParkingSpotScreen(
            onBackClick = { currentScreen = "home" }
        )

        "details" -> ParkingDetailsScreen(
            onBackClick = { currentScreen = "home" }
        )
    }
}