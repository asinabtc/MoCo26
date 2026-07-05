package com.example.mocopraktikum.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mocopraktikum.data.UserPreferencesRepository
import com.example.mocopraktikum.model.ParkingSpot
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun OSMMapView(
    spots: List<ParkingSpot>,
    onSpotClick: (ParkingSpot) -> Unit,
    modifier: Modifier = Modifier,
    mapCenterEvent: SharedFlow<GeoPoint>? = null,
    onMapMoved: (GeoPoint) -> Unit = {},
    followLocation: Boolean = false,
    userPreferencesRepository: UserPreferencesRepository? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(52.5200, 13.4050)) // Default Berlin
            
            addMapListener(DelayedMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    onMapMoved(mapCenter as GeoPoint)
                    return true
                }
                override fun onZoom(event: ZoomEvent?): Boolean {
                    onMapMoved(mapCenter as GeoPoint)
                    return true
                }
            }, 500))
        }
    }

    // Sofortiger Sprung zum letzten Standort oder aktuellen Standort
    LaunchedEffect(Unit) {
        // 1. Letzten gespeicherten Ort versuchen
        val lastLoc = userPreferencesRepository?.lastLocation?.firstOrNull()
        if (lastLoc != null) {
            mapView.controller.setCenter(GeoPoint(lastLoc.first, lastLoc.second))
        }

        // 2. Aktuellen Standort über FusedLocationProvider abfragen für schnellere Reaktion als MyLocationOverlay Fix
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val point = GeoPoint(it.latitude, it.longitude)
                    mapView.controller.animateTo(point)
                    if (followLocation) {
                        // Triggere initiale Suche im Bereich falls ViewModel das braucht
                        onMapMoved(point)
                    }
                }
            }
        } catch (e: SecurityException) { }
    }

    LaunchedEffect(mapCenterEvent) {
        mapCenterEvent?.collect { point ->
            mapView.controller.animateTo(point)
            mapView.controller.setZoom(16.0)
        }
    }

    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            // Automatisch zum Nutzer springen, sobald die erste Position bekannt ist
            runOnFirstFix {
                mapView.post {
                    if (followLocation) {
                        mapView.controller.animateTo(myLocation)
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    myLocationOverlay.enableMyLocation()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    myLocationOverlay.disableMyLocation()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            myLocationOverlay.disableMyLocation()
        }
    }

    AndroidView(
        factory = { 
            mapView.overlays.add(myLocationOverlay)
            mapView 
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            if (followLocation) myLocationOverlay.enableFollowLocation() 
            else myLocationOverlay.disableFollowLocation()

            val markersToRemove = view.overlays.filterIsInstance<Marker>()
            view.overlays.removeAll(markersToRemove)
            
            spots.forEach { spot ->
                if (spot.latitude != null && spot.longitude != null) {
                    val marker = Marker(view)
                    marker.position = GeoPoint(spot.latitude, spot.longitude)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = spot.title
                    marker.snippet = "${spot.street}, ${spot.city}"
                    
                    val icon = context.getDrawable(org.osmdroid.library.R.drawable.marker_default)?.mutate()
                    icon?.setTint(spot.color.toArgb())
                    marker.icon = icon
                    
                    marker.setOnMarkerClickListener { m, _ ->
                        onSpotClick(spot)
                        m.showInfoWindow()
                        true
                    }
                    view.overlays.add(marker)
                }
            }
            view.invalidate()
        }
    )
}
