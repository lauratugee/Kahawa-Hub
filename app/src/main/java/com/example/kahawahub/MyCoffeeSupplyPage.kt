package com.example.kahawahub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private val SupplyBrown = Color(0xFF684832)
private val SupplyGreen = Color(0xFF245640)
private val SupplyCream = Color(0xFFF7F3E9)


@Composable
fun MyCoffeeSupplyPage() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SupplyCream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ){
        Text(
            text = "My Coffee Supply",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SupplyBrown
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Manage your coffee supply",
            color = Color.DarkGray,
            fontSize = 14.sp

        )
    }

}