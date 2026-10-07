package com.example.kahawahub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun  CooperativeDashboard(){
    val userName = remember { mutableStateOf("Cooperative Society") }

    LaunchedEffect(Unit){
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if(userId != null){
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener{ document ->
                    userName.value=
                        document.getString("name") ?: "Cooperative Society"

                }
        }
    }
    Column(
        modifier=Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text="COOPERATIVE DASHBOARD",
            fontSize=24.sp
        )
        Text(
            text="Welcome, ${userName.value}",
            fontSize=18.sp
        )

    }
}