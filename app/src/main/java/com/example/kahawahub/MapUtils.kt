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

/**
 * Lets us write `doc.get().awaitResult()` inside a coroutine instead of nesting
 * addOnSuccessListener / addOnFailureListener callbacks.
 *
 * It suspends (pauses) the coroutine until the Firebase Task finishes, then either
 * returns the result or throws the Task's exception.
 */
suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    // `this` here is the Task; resume the coroutine with the value on success...
    addOnSuccessListener { cont.resume(it) }
    // ...or resume it by throwing the error on failure
    addOnFailureListener { cont.resumeWithException(it) }
}

/**
 * Turns the free-text locations used across the app ("Nyeri", "Mukurweini, Nyeri"...)
 * into map coordinates (LatLng).
 *
 * Order of preference, used for farmers, cooperatives and factories alike:
 *   1. Exact coordinates already saved on the Firestore document (a manually pinned spot)
 *   2. The device geocoder (Android's built-in place-name lookup)
 *   3. A small built-in county/town table (rough, centre-of-county accuracy)
 * If all three fail the function returns null and callers simply skip that pin.
 */
object LocationResolver {

    // Remembers place names we have already looked up, so the same name isn't geocoded
    // again and again (each lookup is a network call). Thread-safe because lookups run
    // on a background thread. Only successful lookups are stored (null can't be stored).
    private val cache = ConcurrentHashMap<String, LatLng>()

    // Rough rectangle around Kenya. The geocoder sometimes matches a name to a place in
    // another country (e.g. a town with the same name abroad); results outside this box
    // are thrown away.
    private fun isInKenya(p: LatLng) = p.latitude in -4.9..5.6 && p.longitude in 33.8..41.99

    // Fallback table: keyword -> approximate centre of that county/town.
    // Used when the geocoder is unavailable (e.g. emulator without Google Play) or finds nothing.
    // linkedMapOf keeps insertion order, so earlier entries are matched first.
    private val regions = linkedMapOf(
        "nyeri" to LatLng(-0.4169, 36.9515),
        "kericho" to LatLng(-0.3700, 35.2831),
        "kiambu" to LatLng(-1.1714, 36.8356),
        "thika" to LatLng(-1.0388, 37.0834),
        "embu" to LatLng(-0.5311, 37.4590),
        "kirinyaga" to LatLng(-0.5000, 37.3333),
        "murang'a" to LatLng(-0.7172, 37.1528),
        "muranga" to LatLng(-0.7172, 37.1528), // spelling without the apostrophe
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

    /**
     * Step 3 of the lookup order: finds the first table keyword contained in [text]
     * (case-insensitive), e.g. "Karatina, Nyeri County" matches "nyeri".
     */
    fun regionPoint(text: String): LatLng? {
        val clean = text.trim().lowercase()
        if (clean.isBlank()) return null
        return regions.entries.firstOrNull { clean.contains(it.key) }?.value
    }

    /**
     * Step 2 of the lookup order (with step 3 as the fallback): converts [text] to coordinates.
     * Runs the geocoder on a background thread because it does network I/O.
     * Returns null if nothing matches.
     */
    suspend fun geocode(context: Context, text: String): LatLng? {
        val key = text.trim().lowercase()
        if (key.isBlank()) return null
        // Already looked up this session? Reuse it.
        cache[key]?.let { return it }

        val found = withContext(Dispatchers.IO) {
            try {
                // Geocoder.isPresent() is false on devices with no geocoding backend
                if (Geocoder.isPresent()) {
                    Geocoder(context.applicationContext, Locale.getDefault())
                        // Appending ", Kenya" steers ambiguous names to the right country
                        .getFromLocationName("$text, Kenya", 1)
                        ?.firstOrNull()
                        ?.let { LatLng(it.latitude, it.longitude) }
                        // Discard matches that landed outside Kenya
                        ?.takeIf { isInKenya(it) }
                } else null
            } catch (e: Exception) {
                // No network, service error, etc. -> treat as "not found" and use the table
                null
            }
        } ?: regionPoint(key) // geocoder gave nothing -> try the built-in table

        if (found != null) cache[key] = found
        return found
    }

    /**
     * Finds where a farmer's farm is:
     *   a) the pin they saved (farm_lat / farm_lng on their `farmers` document), else
     *   b) their sub-county + county from the `users` document, geocoded, else
     *   c) just the county, geocoded.
     */
    suspend fun farmerPoint(db: FirebaseFirestore, context: Context, farmerId: String): LatLng? {
        if (farmerId.isBlank()) return null

        // (a) saved pin
        try {
            val farmer = db.collection("farmers").document(farmerId).get().awaitResult()
            val lat = farmer.getDouble("farm_lat")
            val lng = farmer.getDouble("farm_lng")
            if (lat != null && lng != null) return LatLng(lat, lng)
        } catch (e: CancellationException) {
            // Never swallow cancellation, or the coroutine can't be stopped properly
            throw e
        } catch (e: Exception) {
            // Couldn't read the farmers doc (e.g. it doesn't exist yet) -> fall through to the profile
        }

        // (b) and (c) typed location from the profile
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

    /**
     * Finds where a cooperative is: the pin saved on its `cooperatives` document
     * (latitude / longitude), else its typed `location` geocoded.
     */
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
