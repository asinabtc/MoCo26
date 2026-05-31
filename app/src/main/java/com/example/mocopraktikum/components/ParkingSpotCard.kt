package com.example.mocopraktikum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParkingSpotCard(
    title: String,
    status: String,
    distance: String,
    price: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .background(Color.LightGray)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Marker(color = color)

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("$distance · $price · Status: $status")
            Text("zuletzt gemeldet vor ca. 4 Min.")
        }
    }
}

@Composable
fun ParkingSpotList(onSpotClick: () -> Unit) {
    Column {
        Text(
            text = "Parkplätze in der Nähe",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(8.dp))

        ParkingSpotCard(
            title = "Bahnhofstraße",
            status = "frei",
            distance = "200 m",
            price = "kostenlos",
            color = Color.Green,
            onClick = onSpotClick
        )

        ParkingSpotCard(
            title = "Innenstadt",
            status = "mäßig besucht",
            distance = "450 m",
            price = "kostenpflichtig",
            color = Color(0xFFFFA500),
            onClick = onSpotClick
        )

        ParkingSpotCard(
            title = "Supermarkt Parkplatz",
            status = "frei",
            distance = "700 m",
            price = "kostenlos",
            color = Color.Green,
            onClick = onSpotClick
        )
    }
}