package com.example.kahawahub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfilePage(onBackClick: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Column(
        modifier= Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ){
            TextButton(onBackClick){
                Text("Back")
            }
            Text(
                text="My Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text="Manage your personal information",
            style= MaterialTheme.typography.headlineMedium
        )
        OutlinedTextField(
            value=fullName,
            onValueChange = {fullName=it},
            label={Text("Full Name")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value=phoneNumber,
            onValueChange = {phoneNumber=it},
            label={Text("Phone Number")},
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        HorizontalDivider()

        Text(
            text="Account Information",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text("Email address will appear here")
        Text("Account type will appear here")

        HorizontalDivider()

        Text(
            text="My Cooperative",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text="Your membership will appear here"
        )

    }
}