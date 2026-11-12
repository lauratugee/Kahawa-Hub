package com.example.kahawahub

data class MarketRecommendation(
    val offerId: String,
    val buyerName: String,
    val coffeeType: String,
    val grade: String,
    val offeredPricePerKg: Double,
    val minQuantity: Int,
    val maxQuantity: Int,
    val transportCosts: Double,
    val otherCosts: Double,
    val netReturnPerKg: Double,
    val totalEstimatedReturn: Double,
    val matchScore: Int,
    val recommendationReason: String,
    val location: String,
    val validityDate: String
)

object MarketAnalyzer {

    private fun calculateTransportCost(farmerLocation: String, buyerLocation: String): Double {
        val fLoc = farmerLocation.trim().lowercase()
        val bLoc = buyerLocation.trim().lowercase()

        return if (fLoc.isBlank() || bLoc.isBlank()) {
            10.0
        } else if (fLoc == bLoc || fLoc.contains(bLoc) || bLoc.contains(fLoc)) {
           7.50
        } else {
            30.00
        }
    }

    fun analyzeAndRankOpportunities(
        verifiedSubmissions: List<Map<String, Any>>,
        marketOffers: List<MarketOfferItem>
    ): List<MarketRecommendation> {
        val recommendations = mutableListOf<MarketRecommendation>()

        // 1. Calculate total verified kg
        val totalVerifiedKg = verifiedSubmissions
            .filter { sub ->
                val status = (sub["status"] as? String) ?: ""
                status.equals("Verified", ignoreCase = true)
            }
            .sumOf { sub ->
                (sub["verified_quantity_kg"] as? Long) ?: (sub["quantity_kg"] as? Long) ?: 0L
            }

        // 2. Determine primary verified coffee type & farmer location
        val primarySubmission = verifiedSubmissions.firstOrNull { sub ->
            val status = (sub["status"] as? String) ?: ""
            status.equals("Verified", ignoreCase = true)
        }

        val primaryCoffeeType = primarySubmission?.get("coffee_type") as? String ?: "Coffee Cherries"
        val farmerLocation = primarySubmission?.get("location") as? String ?: ""

        if (totalVerifiedKg <= 0L || marketOffers.isEmpty()) {
            return emptyList()
        }

        // 3. Evaluate each market offer
        for (offer in marketOffers) {
            val isTypeCompatible = offer.coffeeType.equals(primaryCoffeeType, ignoreCase = true) ||
                    offer.coffeeType.contains(primaryCoffeeType, ignoreCase = true) ||
                    primaryCoffeeType.contains(offer.coffeeType, ignoreCase = true)

            // Calculate dynamic transport cost based on region distance
            val estimatedTransportCost = calculateTransportCost(farmerLocation, offer.location)

            val netReturn = offer.offeredPricePerKg - estimatedTransportCost - offer.otherCosts
            val totalReturn = netReturn * totalVerifiedKg

            val minQty = offer.minQuantity.toDouble()
            val maxQty = if (offer.maxQuantity > 0) offer.maxQuantity.toDouble() else Double.MAX_VALUE
            val isQtyValid = totalVerifiedKg.toDouble() in minQty..maxQty

            var score = 50
            val reasons = mutableListOf<String>()

            if (isTypeCompatible) {
                score += 25
                reasons.add("Exact match for your verified $primaryCoffeeType.")
            } else {
                score -= 20
                reasons.add("Alternative coffee type match.")
            }

            if (isQtyValid) {
                score += 20
                reasons.add("Your supply of ${totalVerifiedKg}kg falls within buyer's required range (${offer.minQuantity}kg - ${offer.maxQuantity}kg).")
            } else {
                reasons.add("Quantity differs from buyer's preferred range.")
            }

            // Proximity bonus based on region matching
            val isLocalMatch = farmerLocation.isNotBlank() && offer.location.isNotBlank() &&
                    (farmerLocation.equals(offer.location, ignoreCase = true) ||
                            farmerLocation.contains(offer.location, ignoreCase = true) ||
                            offer.location.contains(farmerLocation, ignoreCase = true))

            if (isLocalMatch) {
                score += 15
                reasons.add("Local proximity match (${offer.location}) ensures lower transport overhead (KES %.2f/kg).".format(estimatedTransportCost))
            } else {
                reasons.add("Regional transport cost estimated at KES %.2f/kg for distance to ${offer.location}.".format(estimatedTransportCost))
            }

            if (netReturn > 0) {
                reasons.add("Estimated net return: KES %.2f per kg.".format(netReturn))
            } else {
                reasons.add("Warning: Transport & costs exceed offered price.")
            }

            recommendations.add(
                MarketRecommendation(
                    offerId = offer.id,
                    buyerName = offer.buyerName,
                    coffeeType = offer.coffeeType,
                    grade = offer.grade,
                    offeredPricePerKg = offer.offeredPricePerKg,
                    minQuantity = offer.minQuantity,
                    maxQuantity = offer.maxQuantity,
                    transportCosts = estimatedTransportCost,
                    otherCosts = offer.otherCosts,
                    netReturnPerKg = netReturn,
                    totalEstimatedReturn = totalReturn,
                    matchScore = score,
                    recommendationReason = reasons.joinToString(" "),
                    location = offer.location,
                    validityDate = offer.validityDat
                )
            )
        }

        // 4. Sort by highest net return then match score
        return recommendations.sortedWith(
            compareByDescending<MarketRecommendation> { it.netReturnPerKg }
                .thenByDescending { it.matchScore }
        )
    }
}
