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

        // 2. Determine primary verified coffee type
        val primaryCoffeeType = verifiedSubmissions
            .firstOrNull { sub ->
                val status = (sub["status"] as? String) ?: ""
                status.equals("Verified", ignoreCase = true)
            }
            ?.get("coffee_type") as? String ?: "Coffee Cherries"

        if (totalVerifiedKg <= 0L || marketOffers.isEmpty()) {
            return emptyList()
        }

        // 3. Evaluate each market offer
        for (offer in marketOffers) {
            val isTypeCompatible = offer.coffeeType.equals(primaryCoffeeType, ignoreCase = true) ||
                    offer.coffeeType.contains(primaryCoffeeType, ignoreCase = true) ||
                    primaryCoffeeType.contains(offer.coffeeType, ignoreCase = true)

            val netReturn = offer.offeredPricePerKg - offer.transportCosts - offer.otherCosts
            val totalReturn = netReturn * totalVerifiedKg

            val minQty = offer.minQuantity.toDouble()
            val maxQty = if (offer.maxQuantity > 0) offer.maxQuantity.toDouble() else Double.MAX_VALUE
            val isQtyValid = totalVerifiedKg.toDouble() in minQty..maxQty

            var score = 50
            val reasons = mutableListOf<String>()

            if (isTypeCompatible) {
                score += 30
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

            if (netReturn > 0) {
                reasons.add("Estimated net return: KES %.2f per kg after transport & costs.".format(netReturn))
            } else {
                reasons.add("Warning: Costs exceed offered price.")
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
                    transportCosts = offer.transportCosts,
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
