package com.example.mocopraktikum.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mocopraktikum.components.Header
import com.example.mocopraktikum.components.MapPlaceholder
import com.example.mocopraktikum.components.ParkingSpotList
import com.example.mocopraktikum.components.SearchField
import com.example.mocopraktikum.components.ViewToggle
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme

@Composable
fun HomeScreen(
    spots: List<ParkingSpot>,
    isLoading: Boolean,
    onAddClick: () -> Unit,
    onSpotClick: (ParkingSpot) -> Unit
) {
    var selectedView by remember { mutableStateOf("Karte") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "+",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {

            Header(title = "ParkSpotter")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Finde freie Parkplätze in deiner Nähe",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            SearchField()

            Spacer(modifier = Modifier.height(24.dp))

            ViewToggle(
                selectedView = selectedView,
                onViewSelected = { selectedView = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    AnimatedContent(
                        targetState = selectedView,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "ViewSwitch"
                    ) { targetView ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth()
                                    .heightIn(min = 400.dp)
                            ) {
                                if (targetView == "Karte") {
                                    MapPlaceholder(onSpotClick = { /* placeholder */ })
                                } else {
                                    ParkingSpotList(
                                        spots = spots,
                                        onSpotClick = onSpotClick
                                    )
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
        HomeScreen(
            spots = emptyList(),
            isLoading = false,
            onAddClick = {},
            onSpotClick = {}
        )
    }
}
