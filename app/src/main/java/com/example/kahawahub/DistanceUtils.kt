package com.example.kahawahub

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * Distance and transport-cost maths shared by the map and the payout dialog.
 */
object DistanceUtils {

    /**
     * Default haulage rate in KES per kg of coffee per km.
     * Example: a 100 km leg at 0.02 costs 100 * 0.02 = KES 2.00 per kg.
     * Cooperatives can override this per settlement in the payout dialog.
     */
    const val DEFAULT_RATE_KES_PER_KG_KM = 0.02

    /**
     * Straight-line ("as the crow flies") distance in kilometres between two points,
     * using the Haversine formula, which accounts for the curvature of the Earth.
     * Real road distance will be longer.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in kilometres
        val dLat = Math.toRadians(lat2 - lat1) // difference in latitude, in radians
        val dLon = Math.toRadians(lon2 - lon1) // difference in longitude, in radians
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a)) // angular distance between the points
        return r * c
    }

    /** Convenience overload so callers can pass two map points directly. */
    fun calculateDistanceKm(from: LatLng, to: LatLng): Double =
        calculateDistanceKm(from.latitude, from.longitude, to.latitude, to.longitude)

    /**
     * Transport cost in KES per kg of coffee for one leg of [distanceKm]:
     * distance x rate. Negative inputs are clamped to 0 so a bad value can never
     * produce a negative cost (which would increase the farmer's payout).
     */
    fun transportCostPerKg(distanceKm: Double, rateKesPerKgKm: Double = DEFAULT_RATE_KES_PER_KG_KM): Double =
        max(0.0, distanceKm) * max(0.0, rateKesPerKgKm)
}
