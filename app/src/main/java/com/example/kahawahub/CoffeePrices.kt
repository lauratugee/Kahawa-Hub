package com.example.kahawahub

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CoffeePrices() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var coffeeType by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Update Coffee Price",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = coffeeType,
            onValueChange = {
                coffeeType = it
            },
            label = {
                Text("Coffee type")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = price,
            onValueChange = {
                price = it
            },
            label = {
                Text("Current Price(KES per kg")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                if (coffeeType.isEmpty() || price.isEmpty()) {

                    Toast.makeText(
                        context,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    if (userId == null) {
                        Toast.makeText(
                            context,
                            "User not logged in",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {

                        db.collection("coffee_prices")
                            .whereEqualTo("cooperative_id", userId)
                            .whereEqualTo("coffee_type", coffeeType)
                            .get()
                            .addOnSuccessListener { documents ->

                                val existingDocument =
                                    documents.documents.firstOrNull()

                                val previousPrice =
                                    documents.documents.firstOrNull()
                                        ?.getLong("price") ?: 0L

                                val priceData = hashMapOf<String, Any>()
                                priceData["cooperative_id"] = userId
                                priceData["coffee_type"] = coffeeType
                                priceData["price"] = price.toLong()
                                priceData["previous_price"] = previousPrice
                                priceData["currency"] = "KES"
                                priceData["price_unit"] = "per kg"
                                priceData["date_updated"] = System.currentTimeMillis()
                                priceData["source"] = "Cooperative"


                                if (existingDocument != null) {

                                    existingDocument.reference
                                        .update(priceData)
                                        .addOnSuccessListener {

                                            Toast.makeText(
                                                context,
                                                "Coffee price updated successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            coffeeType = ""
                                            price = ""
                                        }
                                        .addOnFailureListener { exception ->

                                            Toast.makeText(
                                                context,
                                                "Database error: ${exception.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }

                                } else {
                                    db.collection("coffee_prices")
                                        .add(priceData)
                                        .addOnSuccessListener {

                                            Toast.makeText(
                                                context,
                                                "Coffee price added successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            coffeeType = ""
                                            price = ""
                                        }
                                        .addOnFailureListener { exception ->

                                            Toast.makeText(
                                                context,
                                                "Database error: ${exception.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                            }
                            .addOnFailureListener { exception ->

                                Toast.makeText(
                                    context,
                                    "Could not check previous price  ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Price")
        }
    }
}




















