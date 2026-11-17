package com.example.kahawahub

import androidx.compose.foundation.background
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
    var isLoading by remember { mutableStateOf(true) }

    // Fetch platform statistics and cooperatives in real time using Firestore listeners
    LaunchedEffect(Unit) {
        // 1. Listen to cooperatives count & list
        db.collection("cooperatives")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    totalCooperatives = snapshot.size()
                    cooperativesList = snapshot.documents.map { it.data ?: emptyMap() }
                }
            }

        // 2. Listen to farmers count
        db.collection("farmers")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    totalFarmers = snapshot.size()
                }
            }

        // 3. Listen to supply submissions for total volume
        db.collection("coffee_supply_submissions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    totalVolumeSubmittedKg = snapshot.documents
                        .sumOf { it.getLong("quantity_kg") ?: 0L }
                }
            }

        // 4. Listen to market links for completed transactions
        db.collection("market_links")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    totalCompletedLinks = snapshot.documents
                        .count { (it.getString("status") ?: "").equals("Completed", ignoreCase = true) }
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
                    text = "System-wide oversight & platform analytics",
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
            // Platform Overview Statistics Cards (Responsive Grid / Row arrangement)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Cooperatives", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalCooperatives",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminGreen
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Active Farmers", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalFarmers",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminBrown
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Total Volume", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "%,d kg".format(totalVolumeSubmittedKg),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminGreen
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminCardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Completed Sales", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$totalCompletedLinks",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
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
}
