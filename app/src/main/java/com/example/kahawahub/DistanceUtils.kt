package com.example.kahawahub

import kotlin.math.*

object DistanceUtils {
    // Calculates distance in kilometers between two lat/lng points using Haversine formula
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

    // Calculates exact transport cost based on distance in km (e.g., KES 0.50 per kg per km)
    fun calculateTransportCost(distanceKm: Double): Double {
        return max(5.0, distanceKm * 0.50)
    }
}
