package com.example.kahawahub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private val MapBrown = Color(0xFF684832)
private val MapGreen = Color(0xFF245640)
private val MapRed = Color(0xFFD32F2F)
private val MapBlue = Color(0xFF1976D2)
private val MapOrange = Color(0xFFF57C00)

/** A factory / buyer / miller belonging to a cooperative, with its resolved map position. */
data class MapFactory(
    val id: String,
    val name: String,
    val type: String,
    val location: String,
    val point: LatLng?
)

/**
 * Interactive map showing the cooperative, the viewing farmer (if known) and every factory/buyer
 * the cooperative works with, plus distances between them.
 *
 * Pass [onFarmerPinned] (farmer view) or [onCooperativePinned] (cooperative view) to let the user
 * long-press the map to set an exact location instead of relying on the typed-in place name.
 */
@Composable
fun CooperativeMapCard(
    cooperativeId: String,
    cooperativeName: String,
    locationString: String,
    farmerPoint: LatLng? = null,
    cooperativePoint: LatLng? = null,
    onFarmerPinned: ((LatLng) -> Unit)? = null,
    onCooperativePinned: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    // Cooperative position: saved pin, else geocode its location text
    val coopPoint by produceState<LatLng?>(cooperativePoint, cooperativePoint, locationString) {
        value = cooperativePoint ?: LocationResolver.geocode(context, locationString)
    }

    // Factories of this cooperative, live
    var rawFactories by remember(cooperativeId) { mutableStateOf<List<MapFactory>>(emptyList()) }
    DisposableEffect(cooperativeId) {
        if (cooperativeId.isBlank()) {
            onDispose { }
        } else {
            val listener = db.collection("factories_and_buyers")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, _ ->
                    rawFactories = snapshot?.documents?.map { d ->
                        val lat = d.getDouble("latitude")
                        val lng = d.getDouble("longitude")
                        MapFactory(
                            id = d.id,
                            name = d.getString("entity_name") ?: "Factory",
                            type = d.getString("entity_type") ?: "",
                            location = d.getString("location") ?: "",
                            point = if (lat != null && lng != null) LatLng(lat, lng) else null
                        )
                    } ?: emptyList()
                }
            onDispose { listener.remove() }
        }
    }
    // Factories saved without coordinates are placed by geocoding their location text
    val factories by produceState(emptyList<MapFactory>(), rawFactories) {
        value = rawFactories.map { f ->
            if (f.point != null) f else f.copy(point = LocationResolver.geocode(context, f.location))
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-0.4169, 36.9515), 8f)
    }
    var mapLoaded by remember { mutableStateOf(false) }
    var pendingPin by remember { mutableStateOf<LatLng?>(null) }

    val pinHandler = onFarmerPinned ?: onCooperativePinned
    val pinningFarm = onFarmerPinned != null

    // Zoom so every pin is visible
    val allPoints = listOfNotNull(farmerPoint, coopPoint) + factories.mapNotNull { it.point }
    LaunchedEffect(mapLoaded, allPoints) {
        if (!mapLoaded || allPoints.isEmpty()) return@LaunchedEffect
        if (allPoints.size == 1) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(allPoints[0], 11f))
        } else {
            val bounds = LatLngBounds.builder().apply { allPoints.forEach { include(it) } }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    val farmerToCoopKm = if (farmerPoint != null && coopPoint != null)
        DistanceUtils.calculateDistanceKm(farmerPoint, coopPoint!!) else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = false),
                onMapLoaded = { mapLoaded = true },
                onMapLongClick = { if (pinHandler != null) pendingPin = it }
            ) {
                coopPoint?.let { cp ->
                    Marker(
                        state = rememberMarkerState(key = "coop-$cp", position = cp),
                        title = cooperativeName,
                        snippet = "Cooperative (collection centre)",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
                farmerPoint?.let { fp ->
                    Marker(
                        state = rememberMarkerState(key = "farmer-$fp", position = fp),
                        title = "Your farm",
                        snippet = farmerToCoopKm?.let { "%.1f km from the cooperative".format(it) },
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                    coopPoint?.let { cp ->
                        Polyline(points = listOf(fp, cp), color = MapGreen, width = 8f)
                    }
                }
                factories.forEach { f ->
                    val fPoint = f.point ?: return@forEach
                    val km = coopPoint?.let { DistanceUtils.calculateDistanceKm(it, fPoint) }
                    Marker(
                        state = rememberMarkerState(key = "factory-${f.id}-$fPoint", position = fPoint),
                        title = f.name,
                        snippet = listOfNotNull(
                            f.type.takeIf { it.isNotBlank() },
                            km?.let { "%.1f km from cooperative".format(it) }
                        ).joinToString(" · "),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                    coopPoint?.let { cp ->
                        Polyline(
                            points = listOf(cp, fPoint),
                            color = MapOrange,
                            width = 5f,
                            pattern = listOf(Dash(24f), Gap(14f))
                        )
                    }
                }
                pendingPin?.let { p ->
                    Marker(
                        state = rememberMarkerState(key = "pending-$p", position = p),
                        title = "New location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendDot(MapBlue, "You")
                    LegendDot(MapRed, "Cooperative")
                    LegendDot(MapOrange, "Factory / buyer")
                }

                // Farmer -> cooperative distance
                if (farmerToCoopKm != null) {
                    val perKg = DistanceUtils.transportCostPerKg(farmerToCoopKm)
                    Text(
                        text = "📏 You → $cooperativeName: %.1f km (about KES %.2f/kg transport)"
                            .format(farmerToCoopKm, perKg),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MapGreen
                    )
                } else if (onFarmerPinned != null) {
                    Text(
                        text = "📍 We couldn't place your farm. Long-press the map to set your farm location.",
                        fontSize = 12.sp,
                        color = MapBrown
                    )
                }

                // Cooperative -> factories
                if (factories.isNotEmpty()) {
                    Text("Factories & buyers", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MapBrown)
                    factories.forEach { f ->
                        val km = if (coopPoint != null && f.point != null)
                            DistanceUtils.calculateDistanceKm(coopPoint!!, f.point) else null
                        Text(
                            text = "🏭 ${f.name}: " + (km?.let { "%.1f km from the cooperative".format(it) }
                                ?: "location not found"),
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Text(
                    text = "Transport cost is worked out from these distances and deducted from the final payout.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                if (pinHandler != null) {
                    val pending = pendingPin
                    if (pending == null) {
                        Text(
                            text = if (pinningFarm) "Tip: long-press the map to pin your exact farm location."
                            else "Tip: long-press the map to pin your cooperative's exact location.",
                            fontSize = 11.sp,
                            color = MapBlue
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    pinHandler(pending)
                                    pendingPin = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MapGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (pinningFarm) "Save as my farm" else "Save cooperative location", fontSize = 12.sp)
                            }
                            TextButton(onClick = { pendingPin = null }) {
                                Text("Cancel", color = MapBrown, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(label, fontSize = 11.sp, color = Color.DarkGray)
    }
}
