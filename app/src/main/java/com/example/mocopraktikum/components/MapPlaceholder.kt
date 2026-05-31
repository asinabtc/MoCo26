package com.example.mocopraktikum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MapPlaceholder(onSpotClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Text("Karte", fontSize = 22.sp)

        Marker(
            color = Color.Green,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 90.dp, top = 150.dp)
                .clickable { onSpotClick() }
        )

        Marker(
            color = Color(0xFFFFA500),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 120.dp, top = 160.dp)
                .clickable { onSpotClick() }
        )

        Marker(
            color = Color(0xFFFFA500),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 170.dp)
                .clickable { onSpotClick() }
        )

        Marker(
            color = Color.Green,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 140.dp, bottom = 190.dp)
                .clickable { onSpotClick() }
        )

        Text(
            text = "📍",
            fontSize = 36.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun Marker(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color)
    )
}