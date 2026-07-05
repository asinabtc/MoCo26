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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.example.mocopraktikum.components.Marker
import com.example.mocopraktikum.model.ParkingSpotWithReviews
import com.example.mocopraktikum.model.Review
import com.example.mocopraktikum.ui.theme.MoCoPraktikumTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingDetailsScreen(
    spotWithReviews: ParkingSpotWithReviews?,
    onBackClick: () -> Unit,
    onUpdateOccupancy: (String, Float) -> Unit,
    onAddReview: (Int, String) -> Unit,
    onDeleteSpot: (com.example.mocopraktikum.model.ParkingSpot) -> Unit = {}
) {
    val context = LocalContext.current
    val spot = spotWithReviews?.parkingSpot
    val reviews = spotWithReviews?.reviews ?: emptyList()

    if (spot == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Parkplatz nicht gefunden")
        }
        return
    }

    var localOccupancy by remember(spot.id) { mutableFloatStateOf(spot.occupancy) }
    
    // Review State
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Parkplatz löschen") },
            text = { Text("Möchtest du '${spot.title}' wirklich unwiderruflich aus deiner Liste löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSpot(spot)
                        showDeleteDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parkplatzdetails") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Text("🗑️", fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
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

                    DetailRow("Adresse", "${spot.street}, ${spot.city}")
                    DetailRow("Preis", spot.price)

                    if (spot.latitude != null && spot.longitude != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${spot.latitude},${spot.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("📍 Route starten", color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }
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

            // REVIEWS SECTION
            Text("Bewertungen (${reviews.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            if (reviews.isEmpty()) {
                Text(
                    "Noch keine Bewertungen vorhanden.",
                    modifier = Modifier.padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                reviews.sortedByDescending { it.timestamp }.forEach { review ->
                    ReviewItem(review)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Eigene Bewertung schreiben", fontWeight = FontWeight.Bold)
                    
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            IconButton(onClick = { reviewRating = starIndex }) {
                                Text(
                                    text = if (starIndex <= reviewRating) "★" else "☆",
                                    fontSize = 28.sp,
                                    color = if (starIndex <= reviewRating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Deine Erfahrung...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (reviewText.isNotBlank()) {
                                onAddReview(reviewRating, reviewText)
                                reviewText = ""
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Text("Senden")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text("Status aktualisieren", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("frei", "mäßig besucht", "voll").forEach { option ->
                    val isSelected = when(option) {
                        "frei" -> localOccupancy < 0.3f
                        "mäßig besucht" -> localOccupancy >= 0.3f && localOccupancy < 0.8f
                        "voll" -> localOccupancy >= 0.8f
                        else -> false
                    }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            localOccupancy = when(option) {
                                "frei" -> 0.0f
                                "mäßig besucht" -> 0.5f
                                "voll" -> 1.0f
                                else -> 0.0f
                            }
                        },
                        label = { Text(option.replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text("Belegung (genauer)", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = localOccupancy,
                onValueChange = { localOccupancy = it },
                valueRange = 0f..1f,
                steps = 4
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Abbrechen")
                }

                Button(
                    onClick = { 
                        onUpdateOccupancy(spot.id, localOccupancy)
                        onBackClick() 
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Speichern")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(review.timestamp))
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("★".repeat(review.rating), color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = date, style = MaterialTheme.typography.labelSmall)
        }
        Text(text = review.text, style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
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
            spotWithReviews = null,
            onBackClick = {},
            onUpdateOccupancy = { _, _ -> },
            onAddReview = { _, _ -> }
        )
    }
}
