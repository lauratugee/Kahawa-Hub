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

// App colour palette (same browns/greens used on the other screens)
private val MapBrown = Color(0xFF684832)
private val MapGreen = Color(0xFF245640)
private val MapRed = Color(0xFFD32F2F)    // cooperative marker colour in the legend
private val MapBlue = Color(0xFF1976D2)   // farmer marker colour in the legend
private val MapOrange = Color(0xFFF57C00) // factory marker colour in the legend

/** A factory / buyer / miller belonging to a cooperative, with its resolved map position. */
data class MapFactory(
    val id: String,
    val name: String,
    val type: String,
    val location: String,   // the typed location text, kept so we can geocode it if there's no pin
    val point: LatLng?      // null until we have coordinates (saved or geocoded)
)

/**
 * Interactive Google Map showing:
 *   - the cooperative (red pin),
 *   - the viewing farmer (blue pin), if their location is known,
 *   - every factory/buyer the cooperative works with (orange pins),
 * plus the distances between them listed underneath the map.
 *
 * The caller passes the farmer / cooperative positions it already knows. This card loads the
 * factories itself (live from Firestore) so every screen shows the same factory list.
 *
 * Pass [onFarmerPinned] (farmer view) or [onCooperativePinned] (cooperative view) to let the
 * user long-press the map to set an exact location instead of relying on the typed place name.
 * If neither is passed, the map is view-only.
 */
@Composable
fun CooperativeMapCard(
    cooperativeId: String,              // used to find this cooperative's factories
    cooperativeName: String,
    locationString: String,             // typed location, geocoded if no saved pin
    farmerPoint: LatLng? = null,        // null = farmer unknown (e.g. viewing as a cooperative)
    cooperativePoint: LatLng? = null,   // a saved cooperative pin, if one exists
    onFarmerPinned: ((LatLng) -> Unit)? = null,
    onCooperativePinned: ((LatLng) -> Unit)? = null
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    // --- Cooperative position -----------------------------------------------------------
    // produceState runs the lookup off the main thread and updates `coopPoint` when done.
    // Use the saved pin if we were given one, otherwise geocode the typed location.
    val coopPoint by produceState<LatLng?>(cooperativePoint, cooperativePoint, locationString) {
        value = cooperativePoint ?: LocationResolver.geocode(context, locationString)
    }

    // --- Factories ----------------------------------------------------------------------
    // Live listener on this cooperative's factories. Re-runs if the cooperative changes.
    var rawFactories by remember(cooperativeId) { mutableStateOf<List<MapFactory>>(emptyList()) }
    DisposableEffect(cooperativeId) {
        if (cooperativeId.isBlank()) {
            onDispose { }
        } else {
            val listener = db.collection("factories_and_buyers")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, _ ->
                    rawFactories = snapshot?.documents?.map { d ->
                        // Use the saved coordinates when the factory has them
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
            // Stop listening when the card leaves the screen (prevents leaks)
            onDispose { listener.remove() }
        }
    }
    // Factories saved without coordinates (older records, or a failed geocode at save time)
    // are placed by geocoding their typed location. Re-runs whenever rawFactories changes.
    val factories by produceState(emptyList<MapFactory>(), rawFactories) {
        value = rawFactories.map { f ->
            if (f.point != null) f else f.copy(point = LocationResolver.geocode(context, f.location))
        }
    }

    // --- Map camera & pin-dropping state ---------------------------------------------
    // Start centred on central Kenya; we re-centre to fit all pins once the map has loaded.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-0.4169, 36.9515), 8f)
    }
    var mapLoaded by remember { mutableStateOf(false) }       // true once map tiles are ready
    var pendingPin by remember { mutableStateOf<LatLng?>(null) } // spot the user long-pressed, not yet saved

    // Whichever callback was provided decides if pinning is allowed and who is being pinned
    val pinHandler = onFarmerPinned ?: onCooperativePinned
    val pinningFarm = onFarmerPinned != null

    // Zoom and pan so every known pin is visible. Re-runs when the map loads or pins change.
    val allPoints = listOfNotNull(farmerPoint, coopPoint) + factories.mapNotNull { it.point }
    LaunchedEffect(mapLoaded, allPoints) {
        // newLatLngBounds crashes if called before the map has been laid out, hence the mapLoaded check
        if (!mapLoaded || allPoints.isEmpty()) return@LaunchedEffect
        if (allPoints.size == 1) {
            // A single pin: just centre on it (a bounding box around one point has no size)
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(allPoints[0], 11f))
        } else {
            // Build the smallest box containing every pin and fit the camera to it (100px padding)
            val bounds = LatLngBounds.builder().apply { allPoints.forEach { include(it) } }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    // Straight-line distance from the farm to the cooperative, or null if either is unknown
    val farmerToCoopKm = if (farmerPoint != null && coopPoint != null)
        DistanceUtils.calculateDistanceKm(farmerPoint, coopPoint!!) else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // --- The map itself ---------------------------------------------------------
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = false),
                onMapLoaded = { mapLoaded = true },
                // Long-press drops a temporary pin, but only when pinning is enabled for this screen
                onMapLongClick = { if (pinHandler != null) pendingPin = it }
            ) {
                // Cooperative marker (red)
                coopPoint?.let { cp ->
                    Marker(
                        // The key includes the position, so the marker is rebuilt (and moves)
                        // if the position changes, e.g. after the user saves a new pin
                        state = rememberMarkerState(key = "coop-$cp", position = cp),
                        title = cooperativeName,
                        snippet = "Cooperative (collection centre)",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
                // Farmer marker (blue) plus the solid line to the cooperative
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
                // One orange marker per factory, plus a dashed line from the cooperative to it
                factories.forEach { f ->
                    // Skip factories we couldn't place on the map
                    val fPoint = f.point ?: return@forEach
                    val km = coopPoint?.let { DistanceUtils.calculateDistanceKm(it, fPoint) }
                    Marker(
                        state = rememberMarkerState(key = "factory-${f.id}-$fPoint", position = fPoint),
                        title = f.name,
                        // Tapping the marker shows e.g. "Commercial Buyer · 42.3 km from cooperative"
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
                            // Dashed so factory routes look different from the farm route
                            pattern = listOf(Dash(24f), Gap(14f))
                        )
                    }
                }
                // Temporary purple marker where the user just long-pressed (before they confirm)
                pendingPin?.let { p ->
                    Marker(
                        state = rememberMarkerState(key = "pending-$p", position = p),
                        title = "New location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                    )
                }
            }

            // --- Text details under the map -----------------------------------------------
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Colour legend matching the pin colours
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendDot(MapBlue, "You")
                    LegendDot(MapRed, "Cooperative")
                    LegendDot(MapOrange, "Factory / buyer")
                }

                // Farm -> cooperative distance and its estimated transport cost
                if (farmerToCoopKm != null) {
                    // Estimate at the default rate; the real figure is set when the payout is settled
                    val perKg = DistanceUtils.transportCostPerKg(farmerToCoopKm)
                    Text(
                        text = "📏 You → $cooperativeName: %.1f km (about KES %.2f/kg transport)"
                            .format(farmerToCoopKm, perKg),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MapGreen
                    )
                } else if (onFarmerPinned != null) {
                    // Farmer view but we couldn't place them: tell them how to fix it
                    Text(
                        text = "📍 We couldn't place your farm. Long-press the map to set your farm location.",
                        fontSize = 12.sp,
                        color = MapBrown
                    )
                }

                // Cooperative -> each factory distance
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

                // Explains why distances matter to the farmer
                Text(
                    text = "Transport cost is worked out from these distances and deducted from the final payout.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                // --- Pin-setting controls (only when this screen allows pinning) --------
                if (pinHandler != null) {
                    val pending = pendingPin
                    if (pending == null) {
                        // Nothing selected yet: show a hint about how to pin
                        Text(
                            text = if (pinningFarm) "Tip: long-press the map to pin your exact farm location."
                            else "Tip: long-press the map to pin your cooperative's exact location.",
                            fontSize = 11.sp,
                            color = MapBlue
                        )
                    } else {
                        // User has long-pressed: let them confirm or cancel
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    // The caller decides where to save it (farmers doc or cooperatives doc)
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

/** One legend entry: a coloured dot followed by a label. */
@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text(label, fontSize = 11.sp, color = Color.DarkGray)
    }
}
