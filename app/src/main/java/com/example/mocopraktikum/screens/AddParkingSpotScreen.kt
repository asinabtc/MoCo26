package com.example.mocopraktikum.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mocopraktikum.components.ButtonPlaceholder
import com.example.mocopraktikum.components.InputPlaceholder
import com.example.mocopraktikum.components.ScreenTopBar
import com.example.mocopraktikum.components.SearchField
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme

@Composable
fun AddParkingSpotScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        ScreenTopBar(
            title = "Parkplatz eintragen",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        SearchField()

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Karte", fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        InputPlaceholder("Standort")
        InputPlaceholder("Anzahl der Stellplätze")
        InputPlaceholder("Kommentar, z. B. „Auch für große Autos geeignet“")

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ButtonPlaceholder("Kostenpflichtig")
            ButtonPlaceholder("Kostenlos")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ButtonPlaceholder("Bestätigen")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddParkingSpotScreenPreview() {
    MoCoPraktikumTheme {
        AddParkingSpotScreen(
            onBackClick = {}
        )
    }
}