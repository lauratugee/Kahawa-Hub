package com.example.kahawahub

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val ProfileBrown = Color(0xFF684832)
private val ProfileGreen = Color(0xFF245640)
private val ProfileCream = Color(0xFFF7F3E9)

@Composable
fun CooperativeProfilePage(
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val cooperativeId = auth.currentUser?.uid

    var isLoading by remember { mutableStateOf(true) }
    var cooperativeName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var totalVerifiedSupplyKg by remember { mutableStateOf(0L) }
    var totalSoldSupplyKg by remember { mutableStateOf(0L) }
    var totalMarketDemandKg by remember { mutableStateOf(0L) }

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cooperativeId) {
        if (cooperativeId == null) {
            isLoading = false
        } else {
            db.collection("cooperatives")
                .document(cooperativeId)
                .get()
                .addOnSuccessListener { document ->
                    cooperativeName = document.getString("name") ?: ""
                    location = document.getString("location") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Could not load cooperative profile", Toast.LENGTH_LONG).show()
                }

            // 1. Calculate total verified supply from linked farmers
            db.collection("coffee_supply_submissions")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        totalVerifiedSupplyKg = snapshot.documents
                            .filter { (it.getString("status") ?: "").equals("Verified", ignoreCase = true) }
                            .sumOf { it.getLong("verified_quantity_kg") ?: it.getLong("quantity_kg") ?: 0L }
                    }
                }

            // 2. Calculate total sold/completed supply from market links
            db.collection("market_links")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        totalSoldSupplyKg = snapshot.documents
                            .filter { (it.getString("status") ?: "").equals("Completed", ignoreCase = true) }
                            .sumOf { it.getLong("quantity_kg") ?: 0L }
                    }
                }

            // 3. Calculate total market demand from active offers
            db.collection("market_offers")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        totalMarketDemandKg = snapshot.documents
                            .sumOf { it.getLong("max_quantity") ?: it.getLong("min_quantity") ?: 0L }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cooperative Profile & Analytics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ProfileBrown
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ProfileGreen)
            }
        } else {
            // Cooperative Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = cooperativeName.ifBlank { "Cooperative Society" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileBrown
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 Location: ${location.ifBlank { "Not specified" }}",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Cooperative Location Map
            CooperativeMapCard(
                cooperativeName = cooperativeName.ifBlank { "Cooperative Society" },
                locationString = location.ifBlank { "Nyeri" }
            )

            // Supply & Demand Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Coffee Supply & Demand Overview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileBrown
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val availableStock = (totalVerifiedSupplyKg - totalSoldSupplyKg).coerceAtLeast(0L)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Available Stock", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "%,d kg".format(availableStock),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Sold: %,d kg".format(totalSoldSupplyKg), fontSize = 11.sp, color = Color.DarkGray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Total Market Demand", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = "%,d kg".format(totalMarketDemandKg),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileBrown
                            )
                        }
                    }
                }
            }

            // Embedded Reports & Analytics
            CooperativeReportsPage(cooperativeId = cooperativeId)

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Logout")
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showLogoutDialog = false
                    },
                    title = { Text("Logout") },
                    text = {
                        Text("Are you sure you want to logout?")
                    },
                    confirmButton = {
                        TextButton(onClick = onLogoutClick) {
                            Text("Logout", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
