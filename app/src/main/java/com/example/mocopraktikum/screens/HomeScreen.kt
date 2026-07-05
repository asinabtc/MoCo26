package com.example.mocopraktikum.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mocopraktikum.components.Header
import com.example.mocopraktikum.components.OSMMapView
import com.example.mocopraktikum.components.ParkingSpotList
import com.example.mocopraktikum.components.SearchField
import com.example.mocopraktikum.components.ViewToggle
import com.example.mocopraktikum.data.UserPreferencesRepository
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme
import kotlinx.coroutines.flow.SharedFlow
import org.osmdroid.util.GeoPoint

@Composable
fun HomeScreen(
    spots: List<ParkingSpot>,
    isLoading: Boolean,
    mapCenterEvent: SharedFlow<GeoPoint>? = null,
    onSearch: (String) -> Unit = {},
    onSearchInArea: (Double, Double) -> Unit = { _, _ -> },
    onAddClick: () -> Unit,
    onSpotClick: (ParkingSpot) -> Unit,
    onProfileClick: () -> Unit,
    userPreferencesRepository: UserPreferencesRepository? = null
) {
    var selectedView by remember { mutableStateOf("Karte") }
    var followMyLocation by remember { mutableStateOf(true) }
    var currentMapCenter by remember { mutableStateOf<GeoPoint?>(null) }
    var showSearchAreaButton by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("+", fontSize = 32.sp)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 16.dp)) {
            Header(title = "ParkSpotter", onProfileClick = onProfileClick)
            Spacer(modifier = Modifier.height(16.dp))
            ViewToggle(selectedView = selectedView, onViewSelected = { selectedView = it })
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    AnimatedContent(targetState = selectedView, label = "ViewSwitch", modifier = Modifier.fillMaxSize()) { targetView ->
                        Card(modifier = Modifier.fillMaxSize(), shape = MaterialTheme.shapes.extraLarge) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (targetView == "Karte") {
                                    OSMMapView(
                                        spots = spots,
                                        onSpotClick = onSpotClick,
                                        mapCenterEvent = mapCenterEvent,
                                        onMapMoved = { 
                                            currentMapCenter = it
                                            showSearchAreaButton = true 
                                            followMyLocation = false // Disable follow on manual move
                                        },
                                        followLocation = followMyLocation,
                                        userPreferencesRepository = userPreferencesRepository,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    
                                    // Search UI
                                    Column(modifier = Modifier.align(Alignment.TopCenter).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            tonalElevation = 8.dp,
                                            shadowElevation = 4.dp,
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                        ) {
                                            SearchField(onSearch = { 
                                                onSearch(it)
                                                showSearchAreaButton = false
                                            })
                                        }
                                        
                                        if (showSearchAreaButton && currentMapCenter != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { 
                                                    onSearchInArea(currentMapCenter!!.latitude, currentMapCenter!!.longitude)
                                                    showSearchAreaButton = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                            ) {
                                                Text("In diesem Bereich suchen")
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        ParkingSpotList(spots = spots, onSpotClick = onSpotClick)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MoCoPraktikumTheme {
        HomeScreen(spots = emptyList(), isLoading = false, onAddClick = {}, onSpotClick = {}, onProfileClick = {})
    }
}
