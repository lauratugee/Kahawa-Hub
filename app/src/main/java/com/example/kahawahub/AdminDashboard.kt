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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
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

    // Secondary Modal State for Upgraded Farmer Details Pop-Up
    var selectedFarmerDetailsModal by remember { mutableStateOf<Map<String, Any>?>(null) }
    var farmerCoopName by remember { mutableStateOf("Not assigned") }
    var farmerCoopLocation by remember { mutableStateOf("N/A") }
    var farmerSubmissionsList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var farmerTotalEarnings by remember { mutableStateOf(0.0) }
    var isFarmerDetailsLoading by remember { mutableStateOf(false) }

    // System Broadcast Dialog State
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var adminBroadcastTitle by remember { mutableStateOf("") }
    var adminBroadcastMessage by remember { mutableStateOf("") }

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

        // 2. Listen to active farmers from users collection where user_type == Farmer
        db.collection("users")
            .whereEqualTo("user_type", "Farmer")
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
        // Top Header Row with Title, Broadcast Button, and Logout button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Administrator Portal",
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showBroadcastDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("📢 Broadcast", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Logout", fontSize = 12.sp)
                }
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

    // System Broadcast Dialog
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = {
                Text(
                    text = "📢 Broadcast System Notice",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminBrown
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Publish a platform-wide notice to all farmers and cooperatives.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = adminBroadcastTitle,
                        onValueChange = { adminBroadcastTitle = it },
                        label = { Text("Notice Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = adminBroadcastMessage,
                        onValueChange = { adminBroadcastMessage = it },
                        label = { Text("Notice Message") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminBroadcastTitle.isBlank() || adminBroadcastMessage.isBlank()) {
                            android.widget.Toast.makeText(context, "Please enter title and message", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val notice = hashMapOf(
                            "title" to adminBroadcastTitle,
                            "message" to adminBroadcastMessage,
                            "date_posted" to System.currentTimeMillis(),
                            "is_system_broadcast" to true
                        )
                        db.collection("announcements").add(notice)
                            .addOnSuccessListener {
                                android.widget.Toast.makeText(context, "System broadcast published!", android.widget.Toast.LENGTH_SHORT).show()
                                adminBroadcastTitle = ""
                                adminBroadcastMessage = ""
                                showBroadcastDialog = false
                            }
                            .addOnFailureListener { e ->
                                android.widget.Toast.makeText(context, "Failed: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel", color = AdminBrown)
                }
            }
        )
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
                                    val farmerId = farmer["id"] as? String ?: ""
                                    val name = farmer["name"] as? String ?: farmer["full_name"] as? String ?: "Farmer"
                                    val email = farmer["email"] as? String ?: "No email"
                                    val phone = farmer["phone_number"] as? String ?: "No phone"

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedFarmerDetailsModal = farmer
                                                isFarmerDetailsLoading = true
                                                farmerCoopName = "Loading..."
                                                farmerCoopLocation = ""
                                                farmerSubmissionsList = emptyList()
                                                farmerTotalEarnings = 0.0

                                                db.collection("farmers").document(farmerId).get()
                                                    .addOnSuccessListener { farmerDoc ->
                                                        val coopId = farmerDoc.getString("cooperative_id")
                                                        if (!coopId.isNullOrBlank()) {
                                                            db.collection("cooperatives").document(coopId).get()
                                                                .addOnSuccessListener { coopDoc ->
                                                                    farmerCoopName = coopDoc.getString("name") ?: coopDoc.getString("cooperative_name") ?: "Cooperative"
                                                                    farmerCoopLocation = coopDoc.getString("location") ?: "N/A"
                                                                }
                                                        } else {
                                                            farmerCoopName = "No cooperative assigned"
                                                            farmerCoopLocation = "N/A"
                                                        }
                                                    }

                                                db.collection("coffee_supply_submissions")
                                                    .whereEqualTo("farmer_id", farmerId)
                                                    .get()
                                                    .addOnSuccessListener { subSnapshot ->
                                                        val subs = subSnapshot.documents.map { it.data ?: emptyMap() }
                                                        farmerSubmissionsList = subs
                                                        farmerTotalEarnings = subs.sumOf { (it["final_payout_amount"] as? Double) ?: 0.0 }
                                                        isFarmerDetailsLoading = false
                                                    }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3E9))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(text = name, fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 14.sp)
                                            Text(text = "Email: $email | Phone: $phone", fontSize = 12.sp, color = Color.DarkGray)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "Tap for full profile & history →", fontSize = 11.sp, color = AdminGreen, fontWeight = FontWeight.SemiBold)
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

    // Upgraded Farmer Details Modal (Pop-Up)
    if (selectedFarmerDetailsModal != null) {
        val f = selectedFarmerDetailsModal!!
        val name = f["name"] as? String ?: "Farmer"
        val email = f["email"] as? String ?: "No email"
        val phone = f["phone_number"] as? String ?: "No phone"

        AlertDialog(
            onDismissRequest = { selectedFarmerDetailsModal = null },
            title = {
                Text(
                    text = "Farmer Profile: $name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminBrown
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Personal Information Section
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF8F5))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "👤 Personal Information", fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Name: $name", fontSize = 13.sp, color = Color.DarkGray)
                            Text(text = "Email: $email", fontSize = 13.sp, color = Color.DarkGray)
                            Text(text = "Phone: $phone", fontSize = 13.sp, color = Color.DarkGray)
                        }
                    }

                    // 2. Cooperative Details Section
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF8F5))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "🏢 Cooperative Details", fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isFarmerDetailsLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AdminGreen)
                            } else {
                                Text(text = "Cooperative: $farmerCoopName", fontSize = 13.sp, color = Color.DarkGray)
                                Text(text = "Location: $farmerCoopLocation", fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    // 3. Financial Summary Section
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "💰 Financial Summary", fontWeight = FontWeight.Bold, color = AdminGreen, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total Earnings from Settlements: KES %.2f".format(farmerTotalEarnings),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminGreen
                            )
                        }
                    }

                    // 4. Coffee Supply History Section
                    Text(text = "🌾 Coffee Supply History", fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 15.sp)
                    if (isFarmerDetailsLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AdminGreen)
                        }
                    } else if (farmerSubmissionsList.isEmpty()) {
                        Text(text = "No supply submissions recorded.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        farmerSubmissionsList.forEach { sub ->
                            val cType = sub["coffee_type"] as? String ?: "Coffee"
                            val declared = sub["quantity_kg"] as? Long ?: 0L
                            val verified = sub["verified_quantity_kg"] as? Long ?: 0L
                            val status = sub["status"] as? String ?: "Pending"
                            val payout = sub["final_payout_amount"] as? Double ?: 0.0

                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = cType, fontWeight = FontWeight.Bold, color = AdminBrown, fontSize = 13.sp)
                                    Text(text = "Declared: ${declared} kg | Verified: ${verified} kg", fontSize = 12.sp, color = Color.DarkGray)
                                    Text(text = "Status: $status", fontSize = 12.sp, color = AdminGreen)
                                    if (payout > 0.0) {
                                        Text(text = "Payout: KES %.2f".format(payout), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedFarmerDetailsModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
