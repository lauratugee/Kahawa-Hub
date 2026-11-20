package com.example.kahawahub

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Suspends until a Firebase [Task] finishes. */
suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}

/**
 * Turns the free-text locations used across the app into map coordinates.
 *
 * Order of preference everywhere: coordinates saved on the Firestore document
 * (an exact pin) -> device geocoder -> built-in county/town table.
 */
object LocationResolver {

    private val cache = ConcurrentHashMap<String, LatLng>()

    // Rough bounding box of Kenya; geocoder hits outside it are ambiguous names and are ignored.
    private fun isInKenya(p: LatLng) = p.latitude in -4.9..5.6 && p.longitude in 33.8..41.99

    private val regions = linkedMapOf(
        "nyeri" to LatLng(-0.4169, 36.9515),
        "kericho" to LatLng(-0.3700, 35.2831),
        "kiambu" to LatLng(-1.1714, 36.8356),
        "thika" to LatLng(-1.0388, 37.0834),
        "embu" to LatLng(-0.5311, 37.4590),
        "kirinyaga" to LatLng(-0.5000, 37.3333),
        "murang'a" to LatLng(-0.7172, 37.1528),
        "muranga" to LatLng(-0.7172, 37.1528),
        "bomet" to LatLng(-0.7813, 35.3444),
        "kisii" to LatLng(-0.6817, 34.7667),
        "meru" to LatLng(0.0463, 37.6499),
        "tharaka" to LatLng(-0.2967, 37.9167),
        "machakos" to LatLng(-1.5177, 37.2634),
        "nyandarua" to LatLng(-0.4167, 36.5833),
        "laikipia" to LatLng(0.3606, 36.7819),
        "nairobi" to LatLng(-1.2921, 36.8219),
        "nakuru" to LatLng(-0.3031, 36.0800),
        "kakamega" to LatLng(0.2827, 34.7519),
        "bungoma" to LatLng(0.5635, 34.5606),
        "kisumu" to LatLng(-0.0917, 34.7680),
        "mombasa" to LatLng(-4.0435, 39.6682),
        "taita" to LatLng(-3.3961, 38.5610)
    )

    /** Fallback lookup in the built-in county/town table. */
    fun regionPoint(text: String): LatLng? {
        val clean = text.trim().lowercase()
        if (clean.isBlank()) return null
        return regions.entries.firstOrNull { clean.contains(it.key) }?.value
    }

    /** Geocodes [text]; falls back to the region table. Null if nothing matches. */
    suspend fun geocode(context: Context, text: String): LatLng? {
        val key = text.trim().lowercase()
        if (key.isBlank()) return null
        cache[key]?.let { return it }

        val found = withContext(Dispatchers.IO) {
            try {
                if (Geocoder.isPresent()) {
                    Geocoder(context.applicationContext, Locale.getDefault())
                        .getFromLocationName("$text, Kenya", 1)
                        ?.firstOrNull()
                        ?.let { LatLng(it.latitude, it.longitude) }
                        ?.takeIf { isInKenya(it) }
                } else null
            } catch (e: Exception) {
                null
            }
        } ?: regionPoint(key)

        if (found != null) cache[key] = found
        return found
    }

    /** Farm pin saved by the farmer, else sub-county/county from their profile. */
    suspend fun farmerPoint(db: FirebaseFirestore, context: Context, farmerId: String): LatLng? {
        if (farmerId.isBlank()) return null
        try {
            val farmer = db.collection("farmers").document(farmerId).get().awaitResult()
            val lat = farmer.getDouble("farm_lat")
            val lng = farmer.getDouble("farm_lng")
            if (lat != null && lng != null) return LatLng(lat, lng)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // fall through to profile lookup
        }
        try {
            val user = db.collection("users").document(farmerId).get().awaitResult()
            val sub = user.getString("sub_county").orEmpty()
            val county = user.getString("county").orEmpty()
            return geocode(context, listOf(sub, county).filter { it.isNotBlank() }.joinToString(", "))
                ?: geocode(context, county)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
    }

    /** Cooperative pin saved on its document, else geocoded from its location text. */
    suspend fun cooperativePoint(db: FirebaseFirestore, context: Context, cooperativeId: String): LatLng? {
        if (cooperativeId.isBlank()) return null
        return try {
            val coop = db.collection("cooperatives").document(cooperativeId).get().awaitResult()
            val lat = coop.getDouble("latitude")
            val lng = coop.getDouble("longitude")
            if (lat != null && lng != null) LatLng(lat, lng)
            else geocode(context, coop.getString("location").orEmpty())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
