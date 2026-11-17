package com.example.kahawahub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

private val AdminBrown = Color(0xFF684832)
private val AdminGreen = Color(0xFF245640)
private val AdminCream = Color(0xFFF7F3E9)
private val AdminCardBg = Color(0xFFFFFFFF)

@Composable
fun AdminDashboard(
    onLogoutClick: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // State variables to hold platform-wide statistics
    var totalCooperatives by remember { mutableStateOf(0) }
    var totalFarmers by remember { mutableStateOf(0) }
    var totalVolumeSubmittedKg by remember { mutableStateOf(0L) }
    var totalCompletedLinks by remember { mutableStateOf(0) }

    var cooperativesList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var farmersList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var submissionsList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var salesList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }

    // Drill-down dialog state: null, "cooperatives", "farmers", "submissions", "sales"
    var selectedDrillDownView by remember { mutableStateOf<String?>(null) }

    // Fetch platform statistics and drill-down lists in real time using Firestore listeners
    LaunchedEffect(Unit) {
        // 1. Listen to cooperatives
        db.collection("cooperatives")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    totalCooperatives = snapshot.size()
                    cooperativesList = snapshot.documents.map { doc ->
                        val map = doc.data?.toMutableMap() ?: mutableMapOf()
                        map["id"] = doc.id
                        map
                    }
                }
            }

        // 2. Listen to farmers
        db.collection("farmers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    totalFarmers = snapshot.size()
                    farmersList = snapshot.documents.map { doc ->
                        val map = doc.data?.toMutableMap() ?: mutableMapOf()
                        map["id"] = doc.id
                        map
                    }
                }
            }

        // 3. Listen to supply submissions
        db.collection("coffee_supply_submissions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    submissionsList = snapshot.documents.map { doc ->
                        val map = doc.data?.toMutableMap() ?: mutableMapOf()
                        map["id"] = doc.id
                        map
                    }
                    totalVolumeSubmittedKg = submissionsList.sumOf { (it["quantity_kg"] as? Long) ?: 0L }
                }
            }

        // 4. Listen to market links for completed transactions
        db.collection("market_links")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val allLinks = snapshot.documents.map { doc ->
                        val map = doc.data?.toMutableMap() ?: mutableMapOf()
                        map["id"] = doc.id
                        map
                    }
                    salesList = allLinks.filter { (it["status"] as? String ?: "").equals("Completed", ignoreCase = true) }
                    totalCompletedLinks = salesList.size
                    isLoading = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminCream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Row with Title and Logout button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "👑 Administrator Portal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminBrown
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap any card below to view detailed records",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
            OutlinedButton(
                onClick = onLogoutClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("Logout")
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AdminGreen)
            }
        } else {
            // Interactive Platform Overview Statistics Cards (Tap to Drill Down)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDrillDownView = "cooperatives" },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Cooperatives 📋", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalCooperatives",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap to view list", fontSize = 10.sp, color = Color.DarkGray)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDrillDownView = "farmers" },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Active Farmers 👥", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalFarmers",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap to view list", fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDrillDownView = "submissions" },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Total Volume 🌾", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "%,d kg".format(totalVolumeSubmittedKg),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap to view submissions", fontSize = 10.sp, color = Color.DarkGray)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedDrillDownView = "sales" },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Completed Sales 🤝", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalCompletedLinks",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Tap to view sales", fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Registered Cooperatives Directory Section
            Text(
                text = "🏢 Registered Cooperatives Directory",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AdminBrown
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (cooperativesList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg)
                ) {
                    Text(
                        text = "No cooperatives registered in the system yet.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                cooperativesList.forEach { coop ->
                    val coopName = coop["name"] as? String ?: coop["cooperative_name"] as? String ?: "Unnamed Cooperative"
                    val coopLocation = coop["location"] as? String ?: "Location N/A"
                    val coopEmail = coop["email"] as? String ?: "No email"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = coopName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminBrown
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📍 Location: $coopLocation",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📧 Email: $coopEmail",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }

    // Drill-Down Dialog for Statistics Cards
    if (selectedDrillDownView != null) {
        AlertDialog(
            onDismissRequest = { selectedDrillDownView = null },
            title = {
                Text(
                    text = when (selectedDrillDownView) {
                        "cooperatives" -> "Registered Cooperatives ($totalCooperatives)"
                        "farmers" -> "Registered Farmers ($totalFarmers)"
                        "submissions" -> "Coffee Supply Submissions"
                        "sales" -> "Completed Market Sales ($totalCompletedLinks)"
                        else -> "Details"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminBrown
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedDrillDownView) {
                        "cooperatives" -> {
                            if (cooperativesList.isEmpty()) {
                                Text("No cooperatives found.", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                cooperativesList.forEach { coop ->
                                    val name = coop["name"] as? String ?: coop["cooperative_name"] as? String ?: "Cooperative"
                                    val loc = coop["location"] as? String ?: "N/A"
                                    val phone = coop["phone"] as? String ?: coop["contact_number"] as? String ?: "N/A"
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3E9))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(text = name, fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 14.sp)
                                            Text(text = "Location: $loc | Phone: $phone", fontSize = 12.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                        "farmers" -> {
                            if (farmersList.isEmpty()) {
                                Text("No registered farmers found.", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                farmersList.forEach { farmer ->
                                    val farmerId = farmer["farmer_id"] as? String ?: farmer["user_id"] as? String ?: "ID"
                                    val coopId = farmer["cooperative_id"] as? String ?: "Unassigned"
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3E9))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(text = "Farmer ID: ${farmerId.takeLast(8)}...", fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 13.sp)
                                            Text(text = "Cooperative ID: ${if (coopId.length > 8) coopId.takeLast(8) else coopId}", fontSize = 12.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                        "submissions" -> {
                            if (submissionsList.isEmpty()) {
                                Text("No coffee supply submissions found.", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                submissionsList.forEach { sub ->
                                    val farmerName = sub["farmer_name"] as? String ?: "Farmer"
                                    val coffeeType = sub["coffee_type"] as? String ?: "Coffee"
                                    val qty = sub["quantity_kg"] as? Long ?: 0L
                                    val status = sub["status"] as? String ?: "Pending"
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3E9))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(text = "$farmerName - $coffeeType ($qty kg)", fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 13.sp)
                                            Text(text = "Status: $status", fontSize = 12.sp, color = AdminGreen)
                                        }
                                    }
                                }
                            }
                        }
                        "sales" -> {
                            if (salesList.isEmpty()) {
                                Text("No completed sales found.", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                salesList.forEach { sale ->
                                    val farmerName = sale["farmer_name"] as? String ?: "Farmer"
                                    val buyer = sale["buyer_name"] as? String ?: "Buyer"
                                    val coffee = sale["coffee_type"] as? String ?: "Coffee"
                                    val price = sale["offered_price_per_kg"] as? Double ?: 0.0
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3E9))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(text = "Farmer: $farmerName ➔ Buyer: $buyer", fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 13.sp)
                                            Text(text = "Coffee: $coffee @ KES $price/kg", fontSize = 12.sp, color = AdminGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedDrillDownView = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
