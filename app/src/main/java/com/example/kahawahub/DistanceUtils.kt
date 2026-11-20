package com.example.kahawahub

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

object DistanceUtils {

    /**
     * Default haulage rate in KES per kg of coffee per km.
     * (e.g. 100 km at 0.02 = KES 2.00 per kg.) Cooperatives can override it when settling a payout.
     */
    const val DEFAULT_RATE_KES_PER_KG_KM = 0.02

    // Straight-line (Haversine) distance in kilometers between two lat/lng points
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun calculateDistanceKm(from: LatLng, to: LatLng): Double =
        calculateDistanceKm(from.latitude, from.longitude, to.latitude, to.longitude)

    /** Transport cost in KES per kg of coffee for a leg of [distanceKm]. */
    fun transportCostPerKg(distanceKm: Double, rateKesPerKgKm: Double = DEFAULT_RATE_KES_PER_KG_KM): Double =
        max(0.0, distanceKm) * max(0.0, rateKesPerKgKm)
}
