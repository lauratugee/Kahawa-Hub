package com.example.kahawahub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

private val ReportBrown = Color(0xFF684832)
private val ReportGreen = Color(0xFF245640)
private val ReportRed = Color(0xFFD32F2F)
private val ReportOrange = Color(0xFFE65100)
private val ReportBlue = Color(0xFF1976D2)

@Composable
fun CooperativeReportsPage(
    cooperativeId: String?
) {
    var activeMembers by remember { mutableStateOf(0) }
    var membersLeft by remember { mutableStateOf(0) }
    var totalGivenSupplyKg by remember { mutableStateOf(0L) }
    var totalAcceptedSupplyKg by remember { mutableStateOf(0L) }
    var totalRejectedSupplyKg by remember { mutableStateOf(0L) }
    var totalPendingSupplyKg by remember { mutableStateOf(0L) }
    var totalMarketDemandKg by remember { mutableStateOf(0L) }
    var gradeBreakdown by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(cooperativeId) {
        if (cooperativeId.isNullOrBlank()) {
            isLoading = false
            return@LaunchedEffect
        }

        val db = FirebaseFirestore.getInstance()

        // 1. Active members count
        db.collection("farmers")
            .whereEqualTo("cooperative_id", cooperativeId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    activeMembers = snapshot.size()
                }
            }

        // 2. Members who left (tracked via cooperative_history or former members)
        db.collection("cooperative_history")
            .whereEqualTo("cooperative_id", cooperativeId)
            .whereEqualTo("action", "left")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    membersLeft = snapshot.size()
                }
            }

        // 3. Supply submissions & Quality Control Funnel (Given vs Accepted vs Rejected)
        db.collection("coffee_supply_submissions")
            .whereEqualTo("cooperative_id", cooperativeId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val docs = snapshot.documents
                    totalGivenSupplyKg = docs.sumOf { it.getLong("quantity_kg") ?: 0L }
                    totalAcceptedSupplyKg = docs
                        .filter { (it.getString("status") ?: "").equals("Verified", ignoreCase = true) }
                        .sumOf { it.getLong("verified_quantity_kg") ?: it.getLong("quantity_kg") ?: 0L }
                    totalRejectedSupplyKg = docs
                        .filter { (it.getString("status") ?: "").equals("Rejected", ignoreCase = true) }
                        .sumOf { it.getLong("quantity_kg") ?: 0L }
                    totalPendingSupplyKg = docs
                        .filter { 
                            val st = it.getString("status") ?: ""
                            st.equals("Pending", ignoreCase = true) || st.equals("Under Review", ignoreCase = true)
                        }
                        .sumOf { it.getLong("quantity_kg") ?: 0L }

                    // Grade breakdown
                    val grades = mutableMapOf<String, Long>()
                    docs.forEach { doc ->
                        val grade = (doc.getString("grade") ?: "Standard").trim().uppercase()
                        val qty = doc.getLong("quantity_kg") ?: 0L
                        grades[grade] = (grades[grade] ?: 0L) + qty
                    }
                    gradeBreakdown = grades
                    isLoading = false
                }
            }

        // 4. Market demand from active offers
        db.collection("market_offers")
            .whereEqualTo("cooperative_id", cooperativeId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    totalMarketDemandKg = snapshot.documents
                        .sumOf { it.getLong("max_quantity") ?: it.getLong("min_quantity") ?: 0L }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📊 Cooperative Reports & Visual Analytics",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ReportBrown
        )
        Text(
            text = "System-generated database transaction summaries and quality control reports.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ReportGreen)
            }
        } else {
            // Report 1: Supply vs. Demand Visual Comparison Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚖️ Supply vs. Demand Analysis",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ReportBrown
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val maxVal = maxOf(totalAcceptedSupplyKg, totalMarketDemandKg, 100L).toFloat()
                    val supplyFraction = (totalAcceptedSupplyKg.toFloat() / maxVal).coerceIn(0.05f, 1f)
                    val demandFraction = (totalMarketDemandKg.toFloat() / maxVal).coerceIn(0.05f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Supply Bar
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "%,d kg".format(totalAcceptedSupplyKg), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ReportGreen)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height((supplyFraction * 120).dp)
                                    .background(ReportGreen, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Available Supply", fontSize = 12.sp, color = Color.Gray)
                        }

                        // Demand Bar
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "%,d kg".format(totalMarketDemandKg), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ReportBrown)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height((demandFraction * 120).dp)
                                    .background(ReportBrown, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Market Demand", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Report 2: Quality Control Funnel (Given vs Accepted vs Rejected)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔬 Quality Control Inspection Funnel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ReportBrown
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Given Supply: %,d kg".format(totalGivenSupplyKg),
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Proportional Visual Bar
                    val totalQ = maxOf(totalGivenSupplyKg, 1L).toFloat()
                    val accPct = totalAcceptedSupplyKg.toFloat() / totalQ
                    val rejPct = totalRejectedSupplyKg.toFloat() / totalQ
                    val penPct = totalPendingSupplyKg.toFloat() / totalQ

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            if (accPct > 0f) {
                                Box(modifier = Modifier.weight(accPct).fillMaxHeight().background(ReportGreen, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)))
                            }
                            if (penPct > 0f) {
                                Box(modifier = Modifier.weight(penPct).fillMaxHeight().background(ReportOrange))
                            }
                            if (rejPct > 0f) {
                                Box(modifier = Modifier.weight(rejPct).fillMaxHeight().background(ReportRed, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "🟢 Accepted (Verified)", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "%,d kg".format(totalAcceptedSupplyKg), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ReportGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🟠 Pending Review", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "%,d kg".format(totalPendingSupplyKg), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ReportOrange)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "🔴 Rejected", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "%,d kg".format(totalRejectedSupplyKg), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ReportRed)
                        }
                    }
                }
            }

            // Report 3: Member Growth & Retention (Joined vs Left)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👥 Member Growth & Retention Analysis",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ReportBrown
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Active Members Joined", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "$activeMembers",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ReportGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Members Left", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "$membersLeft",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ReportRed
                            )
                        }
                    }
                }
            }

            // Report 4: Coffee Quality Grade Breakdown (AA, AB, PB, C)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "☕ Coffee Quality Grade Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ReportBrown
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (gradeBreakdown.isEmpty()) {
                        Text(text = "No grade data submitted yet", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        gradeBreakdown.forEach { (grade, qty) ->
                            Row(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Grade $grade", fontSize = 14.sp, color = Color.DarkGray)
                                Text(
                                    text = "%,d kg".format(qty),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ReportBrown
                                )
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
