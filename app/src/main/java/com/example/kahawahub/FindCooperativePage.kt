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
import androidx.compose.ui.platform.LocalContext

data class Cooperative(
    val cooperativeId : String = "",
    val name : String = "",
    val location: String = "",
    val contactNumber : String = "",
    val email : String = "",

)

@Composable
fun FindCooperativePage() {
    var cooperatives by remember {
        mutableStateOf<List<Cooperative>>(emptyList())
    }
    val currentUserId=FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    var isLoading by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(Unit) {

        FirebaseFirestore.getInstance()
            .collection("cooperatives")
            .get()
            .addOnSuccessListener { result ->
                cooperatives = result.documents.map { document ->
                    Cooperative(
                        cooperativeId=document.id,
                        name = document.getString("name") ?: "",
                        location = document.getString("location") ?: "",
                        contactNumber = document.getString("contact_number") ?: "",
                        email = document.getString("email") ?: ""
                    )
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Find a cooperative",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Explore cooperatives and find the best one suited for you!",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (cooperatives.isEmpty()) {

            Text(
                text = "No cooperatives available yet"
            )

        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)

            ) {
                items(cooperatives) { cooperative ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = cooperative.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Location: ${cooperative.location}"
                            )
                            Text(
                                text = "Contact: ${cooperative.contactNumber}"
                            )
                            Text(
                                text = "Email: ${cooperative.email}"
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (currentUserId != null){
                                        FirebaseFirestore.getInstance()
                                            .collection("farmers")
                                            .document(currentUserId)
                                            .set(
                                                mapOf(
                                                    "farmer_id" to currentUserId,
                                                    "user_id" to currentUserId,
                                                    "cooperative_id" to cooperative.cooperativeId
                                                ),
                                                com.google.firebase.firestore.SetOptions.merge()
                                            )
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "You have joined ${cooperative.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(
                                                    context,
                                                    "Failed to join ${exception.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                    }

                                },
                            ) {
                                Text("Join Cooperative")
                            }
                        }
                    }
                }
            }
        }
    }
}






