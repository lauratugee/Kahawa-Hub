package com.example.kahawahub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun FarmerDashboard(
    onFindCooperativeClick:() -> Unit,
    onViewCoffeePricesClick: () -> Unit
) {
    val userName=remember {
        mutableStateOf("Farmer")
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {
                        userName.value = document.getString("name") ?: "Farmer"
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "COFFEE MARKET",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Welcome, ${userName.value}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "User type : Farmer",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(25.dp))

        Column(
            modifier = Modifier.fillMaxWidth()

        ) {
            Button(
                onClick={
                    onViewCoffeePricesClick()
                },
                modifier=Modifier.fillMaxWidth()
            ){
                Text("Coffee Prices")
            }
        Spacer(modifier=Modifier.height(12.dp))

            Button(
                onClick={
                    onFindCooperativeClick()
                },
                modifier=Modifier.fillMaxWidth()
            ){
                Text("Find a cooperative")
            }

        }

    }
}