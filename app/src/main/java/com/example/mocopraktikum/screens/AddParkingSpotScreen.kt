package com.example.mocopraktikum.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParkingSpotScreen(
    onBackClick: () -> Unit,
    onConfirmClick: (String, String, String, Boolean) -> Unit
) {
    var location by remember { mutableStateOf("") }
    var spots by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var isPaid by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parkplatz eintragen") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Details zum Parkplatz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Standort / Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = spots,
                onValueChange = { spots = it },
                label = { Text("Anzahl der Stellplätze") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Kommentar") },
                placeholder = { Text("z. B. Auch für große Autos geeignet") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium
            )

            Text(
                text = "Kosten",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = isPaid == true,
                    onClick = { isPaid = true },
                    label = { Text("Kostenpflichtig") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isPaid == false,
                    onClick = { isPaid = false },
                    label = { Text("Kostenlos") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    onConfirmClick(location, spots, comment, isPaid ?: false)
                    onBackClick() 
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                enabled = location.isNotBlank() && isPaid != null
            ) {
                Text("Bestätigen", modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddParkingSpotScreenPreview() {
    MoCoPraktikumTheme {
        AddParkingSpotScreen(
            onBackClick = {},
            onConfirmClick = { _, _, _, _ -> }
        )
    }
}
