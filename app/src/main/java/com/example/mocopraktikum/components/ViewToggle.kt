package com.example.mocopraktikum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ViewToggle(
    selectedView: String,
    onViewSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Karte",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedView == "Karte") Color.LightGray else Color.Transparent)
                .clickable { onViewSelected("Karte") }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 18.sp
        )

        Text(
            text = " | ",
            fontSize = 24.sp
        )

        Text(
            text = "Liste",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedView == "Liste") Color.LightGray else Color.Transparent)
                .clickable { onViewSelected("Liste") }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 18.sp
        )
    }
}