package com.example.mocopraktikum.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mocopraktikum.components.ButtonPlaceholder
import com.example.mocopraktikum.components.Marker
import com.example.mocopraktikum.components.ScreenTopBar
import com.example.mocopraktikum.components.SliderPlaceholder
import com.example.mocopraktikum.components.TextAreaPlaceholder
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme

@Composable
fun ParkingDetailsScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        ScreenTopBar(
            title = "Parkplatzdetails",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text("Bahnhofstraße", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Marker(color = Color.Green)

            Spacer(modifier = Modifier.width(16.dp))

            Text("Status: frei", fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Entfernung: 200 m", fontSize = 20.sp)
        Text("Plätze: ca. 3", fontSize = 20.sp)
        Text("zuletzt gemeldet vor: ca. 4 Min", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(40.dp))

        Text("Kommentar:", fontSize = 20.sp)
        Text("Große Autos haben genug Platz", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(40.dp))

        ButtonPlaceholder("Status ändern")

        Spacer(modifier = Modifier.height(24.dp))

        SliderPlaceholder()

        Spacer(modifier = Modifier.height(32.dp))

        ButtonPlaceholder("Problem melden")

        Spacer(modifier = Modifier.height(16.dp))

        TextAreaPlaceholder()

        Spacer(modifier = Modifier.height(24.dp))

        ButtonPlaceholder("Navigation starten")

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ButtonPlaceholder("Abbrechen")

            Spacer(modifier = Modifier.width(16.dp))

            ButtonPlaceholder("Speichern")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParkingDetailsScreenPreview() {
    MoCoPraktikumTheme {
        ParkingDetailsScreen(
            onBackClick = {}
        )
    }
}