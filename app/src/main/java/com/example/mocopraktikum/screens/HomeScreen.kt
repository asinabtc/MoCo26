package com.example.mocopraktikum.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mocopraktikum.components.Header
import com.example.mocopraktikum.components.MapPlaceholder
import com.example.mocopraktikum.components.ParkingSpotList
import com.example.mocopraktikum.components.SearchField
import com.example.mocopraktikum.components.ViewToggle
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme

@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onSpotClick: () -> Unit
) {
    var selectedView by remember { mutableStateOf("Karte") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Color.LightGray
            ) {
                Text("+", fontSize = 32.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Header(title = "ParkSpotter")

            Spacer(modifier = Modifier.height(24.dp))

            SearchField()

            Spacer(modifier = Modifier.height(24.dp))

            ViewToggle(
                selectedView = selectedView,
                onViewSelected = { selectedView = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedView == "Karte") {
                MapPlaceholder(onSpotClick = onSpotClick)
            } else {
                ParkingSpotList(onSpotClick = onSpotClick)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MoCoPraktikumTheme {
        HomeScreen(
            onAddClick = {},
            onSpotClick = {}
        )
    }
}