package com.example.kahawahub

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale

@Composable
fun CooperativeMapCard(
    cooperativeName: String,
    locationString: String
) {
    val context = LocalContext.current
    var markerLatLng by remember(locationString) {
        mutableStateOf(getLatLngForLocation(locationString, context))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerLatLng, 11f)
    }

    // Update camera when markerLatLng changes
    LaunchedEffect(markerLatLng) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(markerLatLng, 11f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = markerLatLng),
                    title = cooperativeName,
                    snippet = "Location: $locationString"
                )
            }
        }
    }
}

// Helper to convert location name to LatLng (Kenyan regions + Geocoder fallback)
private fun getLatLngForLocation(location: String, context: android.content.Context): LatLng {
    val cleanLoc = location.trim().lowercase()

    // Preset map for common Kenyan coffee counties
    val kenyaCoffeeRegions = mapOf(
        "nyeri" to LatLng(-0.4169, 36.9515),
        "kericho" to LatLng(-0.3700, 35.2831),
        "kiambu" to LatLng(-1.1714, 36.8356),
        "embu" to LatLng(-0.5311, 37.4590),
        "kirinyaga" to LatLng(-0.5000, 37.3333),
        "murang'a" to LatLng(-0.7172, 37.1528),
        "muranga" to LatLng(-0.7172, 37.1528),
        "bomet" to LatLng(-0.7813, 35.3444),
        "kisii" to LatLng(-0.6817, 34.7667),
        "meru" to LatLng(0.0463, 37.6499),
        "nairobi" to LatLng(-1.2921, 36.8219),
        "nakuru" to LatLng(-0.3031, 36.0800)
    )

    for ((region, latLng) in kenyaCoffeeRegions) {
        if (cleanLoc.contains(region)) {
            return latLng
        }
    }

    // Try Android Geocoder if not found in preset
    try {
        if (Geocoder.isPresent()) {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName("$location, Kenya", 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                return LatLng(address.latitude, address.longitude)
            }
        }
    } catch (_: Exception) {}

    // Default fallback (Nyeri)
    return LatLng(-0.4169, 36.9515)
}
