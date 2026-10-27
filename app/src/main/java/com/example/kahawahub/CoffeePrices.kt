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
fun CoffeePrices(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var coffeeType by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ){
        Button(onClick = onBackClick ) {
            Text("Back")
        }
        Spacer(modifier=Modifier.height(12.dp))
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
            modifier = Modifier.fillMaxWidth(),
            singleLine=true,
            enabled=!isSaving
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { input ->
                if(input.isEmpty() || input.all{ it.isDigit()}) {
                    price=input
                }
            },
            label = {
                Text("Current Price(KES per kg")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled=!isSaving
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val cleanedType=coffeeType.trim()
                val newPrice=price.toLongOrNull()

                if (cleanedType.isEmpty() || newPrice==null || newPrice<=0L) {

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
                        isSaving=true

                        db.collection("coffee_prices")
                            .whereEqualTo("cooperative_id", userId)
                            .whereEqualTo("coffee_type", coffeeType.trim())
                            .get()
                            .addOnSuccessListener { documents ->

                                val existingDocuments = documents.documents
                                val existingDocument=existingDocuments.firstOrNull()

                                val previousPrice = existingDocument
                                        ?.getLong("price") ?: 0L

                                val priceData = hashMapOf<String, Any>()
                                priceData["cooperative_id"] = userId
                                priceData["coffee_type"] = coffeeType.trim()
                                priceData["price"] = price.toLong()
                                priceData["previous_price"] = previousPrice
                                priceData["currency"] = "KES"
                                priceData["price_unit"] = "per kg"
                                priceData["date_updated"] = System.currentTimeMillis()
                                priceData["source"] = "Cooperative"


                                if (existingDocument !=null) {
                                    existingDocument.reference
                                        .update(priceData)
                                        .addOnSuccessListener {

                                            val batch = db.batch()

                                            existingDocuments.drop(1).forEach { duplicate ->
                                                batch.delete(duplicate.reference)
                                            }
                                            if (existingDocuments.size > 1) {
                                                batch.commit()
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Coffee price updated successfully and duplicate records removed",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        coffeeType = ""
                                                        price = ""
                                                        isSaving = false
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Toast.makeText(
                                                            context,
                                                            "Database error: ${exception.message}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        isSaving = false
                                                    }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Coffee price updated successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                coffeeType = ""
                                                price = ""
                                                isSaving = false
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(
                                                context,
                                                "Database error: ${exception.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isSaving=false
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
                                            isSaving = false
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(
                                                context,
                                                "Database error: ${exception.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isSaving = false
                                        }
                                }
                            }
                            .addOnFailureListener { exception ->

                                Toast.makeText(
                                    context,
                                    "Could not check previous price  ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                isSaving = false
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if(isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else{
                Text("Update Price")
            }
        }
    }
}




















