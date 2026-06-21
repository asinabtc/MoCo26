package com.example.mocopraktikum.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mocopraktikum.components.Marker
import com.example.mocopraktikum.model.ParkingSpot
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingDetailsScreen(
    spot: ParkingSpot?,
    onBackClick: () -> Unit,
    onUpdateOccupancy: (String, Float) -> Unit
) {
    if (spot == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Parkplatz nicht gefunden")
        }
        return
    }

    var localOccupancy by remember(spot.id) { mutableFloatStateOf(spot.occupancy) }
    var reportText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parkplatzdetails") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = spot.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Marker(color = spot.color)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Status: ${spot.status}",
                            style = MaterialTheme.typography.titleMedium,
                            color = spot.color
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailRow("Entfernung", spot.distance)
                    DetailRow("Preis", spot.price)
                    DetailRow("Gemeldet", "vor ca. 4 Min")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Kommentar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                spot.comment.ifBlank { "Kein Kommentar vorhanden." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            Text("Aktuellen Status melden", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = localOccupancy,
                onValueChange = { localOccupancy = it },
                valueRange = 0f..1f,
                steps = 1
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Leer", style = MaterialTheme.typography.labelMedium)
                Text("Voll", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = reportText,
                onValueChange = { reportText = it },
                label = { Text("Problem melden oder Info hinzufügen") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* Navigation */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Navigation starten")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Abbrechen")
                }

                Button(
                    onClick = { 
                        onUpdateOccupancy(spot.id, localOccupancy)
                        onBackClick() 
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Speichern")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun ParkingDetailsScreenPreview() {
    MoCoPraktikumTheme {
        ParkingDetailsScreen(
            spot = null,
            onBackClick = {},
            onUpdateOccupancy = { _, _ -> }
        )
    }
}
