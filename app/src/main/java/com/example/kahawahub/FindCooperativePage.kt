package com.example.kahawahub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.SetOptions

private val CooperativeBrown = Color(0xFF684832)
private val CooperativeGreen = Color(0xFF245640)
private val CooperativeCream = Color(0xFFF7F3E9)

data class Cooperative(
    val cooperativeId: String = "",
    val name: String = "",
    val location: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val description: String = "",
    val services: String = "",
    val membershipInfo: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindCooperativePage(
    onBackClick: () -> Unit
) {
    var cooperatives by remember { mutableStateOf<List<Cooperative>>(emptyList()) }
    var farmerCooperativeId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCooperativeInfo by remember { mutableStateOf<Cooperative?>(null) }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        val db = FirebaseFirestore.getInstance()

        if (currentUserId != null) {
            db.collection("farmers").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        farmerCooperativeId = document.getString("cooperative_id")
                    }
                }
        }
        db.collection("cooperatives")
            .get()
            .addOnSuccessListener { result ->
                cooperatives = result.documents.map { document ->
                    val rawServices = document.get("services")
                    val servicesStr = when (rawServices) {
                        is List<*> -> rawServices.joinToString("\n• ", prefix = "• ")
                        is String -> rawServices
                        else -> "No services listed."
                    }

                    Cooperative(
                        cooperativeId = document.id,
                        name = document.getString("name") ?: document.getString("cooperative_name") ?: "",
                        location = document.getString("location") ?: "",
                        contactNumber = document.getString("contact_number") ?: "",
                        email = document.getString("email") ?: "",
                        description = document.getString("description") ?: "N/A",
                        services = servicesStr,
                        membershipInfo = document.getString("membership_info") ?: "No membership information provided."
                    )
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    val filteredCooperatives = cooperatives.filter { cooperative ->
        cooperative.name.contains(searchQuery, ignoreCase = true) ||
                cooperative.location.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CooperativeCream)
            .padding(20.dp)
    ) {
        Button(
            onClick = { onBackClick() },
            colors = ButtonDefaults.buttonColors(containerColor = CooperativeGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Back", color = Color.White)
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Find a cooperative",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = CooperativeBrown
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search for a cooperative") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CooperativeGreen)
            }
        } else if (filteredCooperatives.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No cooperatives match your search",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCooperatives) { cooperative ->
                    val isAlreadyJoined = farmerCooperativeId == cooperative.cooperativeId

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cooperative.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CooperativeBrown
                                )
                                if (isAlreadyJoined) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFC8E6C9)
                                    ) {
                                        Text(
                                            text = "Joined",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CooperativeGreen
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Location/Region: ${cooperative.location}",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Contact: ${cooperative.contactNumber}",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedCooperativeInfo = cooperative },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "More Info",
                                        fontSize = 12.sp,
                                        color = CooperativeBrown
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (farmerCooperativeId != null && farmerCooperativeId != cooperative.cooperativeId) {
                                            Toast.makeText(
                                                context,
                                                "You are already a member of a cooperative. You cannot join another.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        if (currentUserId != null) {
                                            FirebaseFirestore.getInstance()
                                                .collection("farmers")
                                                .document(currentUserId)
                                                .set(
                                                    mapOf(
                                                        "farmer_id" to currentUserId,
                                                        "user_id" to currentUserId,
                                                        "cooperative_id" to cooperative.cooperativeId
                                                    ),
                                                    SetOptions.merge()
                                                )
                                                .addOnSuccessListener {
                                                    farmerCooperativeId = cooperative.cooperativeId
                                                    Toast.makeText(
                                                        context,
                                                        "You have successfully joined ${cooperative.name}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to join: ${e.localizedMessage}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isAlreadyJoined,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAlreadyJoined) Color.Gray else CooperativeGreen
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        if (isAlreadyJoined) "Joined" else "Join",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedCooperativeInfo != null) {
        val coop = selectedCooperativeInfo!!

        AlertDialog(
            onDismissRequest = { selectedCooperativeInfo = null },
            title = {
                Text(
                    text = coop.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CooperativeBrown
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Location/Region: ${coop.location}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CooperativeGreen
                    )
                    HorizontalDivider(color = Color.LightGray)

                    Text(
                        text = "About Us",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CooperativeBrown
                    )
                    Text(
                        text = coop.description,
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    HorizontalDivider(color = Color.LightGray)

                    Text(
                        text = "Our Services",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CooperativeBrown
                    )
                    Text(
                        text = coop.services,
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    HorizontalDivider(color = Color.LightGray)

                    Text(
                        text = "Membership Information",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CooperativeBrown
                    )
                    Text(
                        text = coop.membershipInfo,
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    HorizontalDivider(color = Color.LightGray)

                    Text(
                        text = "Contact Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CooperativeBrown
                    )
                    Text(
                        text = "Phone: ${coop.contactNumber} \nEmail: ${coop.email}",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedCooperativeInfo = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CooperativeGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
