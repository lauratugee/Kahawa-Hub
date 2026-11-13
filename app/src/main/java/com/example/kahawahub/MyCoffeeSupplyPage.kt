package com.example.kahawahub

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SupplyBrown = Color(0xFF684832)
private val SupplyGreen = Color(0xFF245640)
private val SupplyCream = Color(0xFFF7F3E9)
private val SupplyRed = Color(0xFFB4483E)
private val SupplyOrange = Color(0xFFE65100)

data class CoffeeSupplyItem(
    val id: String = "",
    val coffeeType: String = "",
    val quantityKg: Int = 0,
    val status: String = "Pending",
    val dateSubmitted: Long = 0L,
    val cooperativeId: String = "",
    val rejectionReason: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoffeeSupplyPage(
    cooperativeId: String? = null
) {
    val context = LocalContext.current

    var selectedCoffeeType by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var coffeeTypeExpanded by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submissionMessage by remember { mutableStateOf("") }
    var isSuccessMessage by remember { mutableStateOf(false) }

    var submissions by remember { mutableStateOf<List<CoffeeSupplyItem>>(emptyList()) }
    var isSubmissionsLoading by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Listen to current farmer's past supply submissions in real-time
    DisposableEffect(userId) {
        if (userId == null) {
            isSubmissionsLoading = false
            onDispose { }
        } else {
            val listener = db.collection("coffee_supply_submissions")
                .whereEqualTo("farmer_id", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("KahawaSupply", "Error fetching submissions", error)
                    }
                    if (snapshot != null) {
                        submissions = snapshot.documents.map { doc ->
                            CoffeeSupplyItem(
                                id = doc.id,
                                coffeeType = doc.getString("coffee_type") ?: "",
                                quantityKg = (doc.getLong("quantity_kg") ?: doc.getLong("quantity") ?: 0L).toInt(),
                                status = doc.getString("status") ?: "Pending",
                                dateSubmitted = doc.getLong("date_submitted") ?: 0L,
                                cooperativeId = doc.getString("cooperative_id") ?: "",
                                rejectionReason = doc.getString("rejection_reason") ?: ""
                            )
                        }.sortedByDescending { it.dateSubmitted }
                    }
                    isSubmissionsLoading = false
                }
            onDispose { listener.remove() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SupplyCream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "My Coffee Supply",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SupplyBrown
        )
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Record and track your coffee supply submissions",
            color = Color.DarkGray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        // New Submission Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "New Supply Entry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SupplyBrown
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Coffee Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SupplyBrown
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = coffeeTypeExpanded,
                    onExpandedChange = {
                        coffeeTypeExpanded = !coffeeTypeExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedCoffeeType,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select coffee type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = coffeeTypeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = coffeeTypeExpanded,
                        onDismissRequest = { coffeeTypeExpanded = false }
                    ) {
                        val coffeeTypes = listOf("Coffee Cherries", "Mbuni", "Pulped Coffee")
                        coffeeTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedCoffeeType = type
                                    coffeeTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quantity (kg)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SupplyBrown
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            quantity = newValue
                        }
                    },
                    placeholder = { Text("e.g. 500") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val kg = quantity.toIntOrNull() ?: 0
                        if (selectedCoffeeType.isBlank() || kg <= 0) {
                            Toast.makeText(context, "Please select coffee type and enter valid quantity", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (userId == null) {
                            Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmitting = true
                        submissionMessage = ""

                        val submissionData = hashMapOf(
                            "farmer_id" to userId,
                            "farmer_name" to (auth.currentUser?.displayName ?: "Farmer"),
                            "coffee_type" to selectedCoffeeType,
                            "quantity_kg" to kg,
                            "status" to "Pending",
                            "date_submitted" to System.currentTimeMillis(),
                            "cooperative_id" to (cooperativeId ?: "")
                        )

                        db.collection("coffee_supply_submissions")
                            .add(submissionData)
                            .addOnSuccessListener {
                                isSubmitting = false
                                isSuccessMessage = true
                                submissionMessage = "Supply submitted successfully!"
                                selectedCoffeeType = ""
                                quantity = ""
                            }
                            .addOnFailureListener { e ->
                                isSubmitting = false
                                isSuccessMessage = false
                                submissionMessage = "Submission failed: ${e.localizedMessage}"
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = SupplyGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Submit Supply", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (submissionMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = submissionMessage,
                        color = if (isSuccessMessage) SupplyGreen else SupplyRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Section
        Text(
            text = "My Submission History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SupplyBrown
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isSubmissionsLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SupplyGreen)
            }
        } else if (submissions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = "No coffee supply submissions recorded yet.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

            submissions.forEach { item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.coffeeType,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SupplyBrown
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Quantity: ${item.quantityKg} kg",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateFormat.format(Date(item.dateSubmitted)),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            // Status Tag
                            val (statusBg, statusFg) = when (item.status.lowercase()) {
                                "verified", "accepted - bring to coop", "milled & settled" -> Color(0xFFC8E6C9) to SupplyGreen
                                "rejected" -> Color(0xFFFFCDD2) to SupplyRed
                                else -> Color(0xFFFFE0B2) to SupplyOrange
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = statusBg,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = item.status,
                                    color = statusFg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }

                        // Visual Progress Stepper
                        SupplyProgressStepper(status = item.status)

                        if (item.status.lowercase() == "rejected" && item.rejectionReason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFFFCDD2), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reason for Rejection: ${item.rejectionReason}",
                                fontSize = 13.sp,
                                color = SupplyRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupplyProgressStepper(status: String) {
    val statusLower = status.lowercase()
    val steps = listOf("Declared", "Delivery", "Gate Verified", "Settled")
    val currentStep = when {
        statusLower.contains("milled") || statusLower.contains("settled") -> 4
        statusLower.contains("verified") -> 3
        statusLower.contains("accepted") || statusLower.contains("brought") -> 2
        statusLower.contains("rejected") -> -1
        else -> 1 // Pending / Declared
    }

    if (currentStep == -1) {
        return
    }

    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, stepName ->
            val stepNumber = index + 1
            val isCompleted = stepNumber <= currentStep
            val color = if (isCompleted) SupplyGreen else Color.LightGray

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = color,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "$stepNumber", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stepName, fontSize = 10.sp, color = if (isCompleted) SupplyBrown else Color.Gray)
            }
        }
    }
}
