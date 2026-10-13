package com.example.kahawahub

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.nio.file.WatchEvent
import com.google.firebase.firestore.FieldValue

@Composable
fun ProfilePage(
    onBackClick: () -> Unit,
    onLogoutClick:() -> Unit
){

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var userType by remember { mutableStateOf("") }
    var cooperativeName by remember { mutableStateOf("") }
    var cooperativeLocation by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        val userId = currentUser?.uid

        if (userId == null) {
            isLoading = false
        } else {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    fullName = document.getString("name") ?: ""
                    phoneNumber = document.getString("phone_number") ?: ""
                    email = document.getString("email") ?: currentUser?.email ?: ""
                    userType = document.getString("user_type") ?: ""
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(
                        context,
                        "Could not load profile",
                        Toast.LENGTH_LONG
                    ).show()
                }
            db.collection("farmers")
                .document(userId)
                .get()
                .addOnSuccessListener { farmerDocument ->

                    val cooperativeId =
                        farmerDocument.getString("cooperative_id")

                    if (!cooperativeId.isNullOrBlank()) {
                        db.collection("cooperatives")
                            .document(cooperativeId)
                            .get()
                            .addOnSuccessListener { cooperativeDocument ->

                                cooperativeName =
                                    cooperativeDocument.getString("name") ?: ""
                                cooperativeLocation =
                                    cooperativeDocument.getString("location") ?: ""
                            }
                    }
                }
        }

    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onBackClick) {
                Text("Back")
            }
            Text(
                text = "My Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Manage your personal information",
            style = MaterialTheme.typography.headlineMedium
        )
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        HorizontalDivider()

        Text(
            text = "Account Information",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = userType,
            onValueChange = {},
            label = { Text("Account Type") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()

        Text(
            text = "My Cooperative",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (userType.equals("Farmer", ignoreCase = true)) {

                if (cooperativeName.isNotBlank()) {
                    "Cooperative: $cooperativeName\n" +
                            "Location: $cooperativeLocation"
                } else {
                    "Not joined a cooperative yet"
                }
            } else {
                "Cooperative details not available"
            }
        )
        if(
            userType=="Farmer" &&
            cooperativeName.isNotBlank()
        ){
            OutlinedButton(
                onClick = {
                    val userId=currentUser?.uid

                    if (userId!=null){
                        db.collection("farmers")
                            .document(userId)
                            .update(
                                "cooperative_id",
                                FieldValue.delete()

                            )
                            .addOnSuccessListener {
                                cooperativeName=""
                                cooperativeLocation=""

                                Toast.makeText(
                                    context,
                                    "You have lefft the cooperative",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Could not leave cooperative",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }

                },
                modifier=Modifier.fillMaxWidth(),
                colors= ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )

            ){
                Text("Leave Cooperative")
            }

        }


        Button(
            onClick = {
                val userId = currentUser?.uid

                if (userId == null) {
                    Toast.makeText(
                        context,
                        "Login again",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (
                    fullName.isBlank() || phoneNumber.isBlank()
                ) {
                    Toast.makeText(
                        context,
                        "Name and Phone number required",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    isSaving = true

                    db.collection("users")
                        .document(userId)
                        .set(
                            mapOf(
                                "name" to fullName.trim(),
                                "phone_number" to phoneNumber.trim()

                            ),
                            SetOptions.merge()
                        )
                        .addOnSuccessListener {
                            isSaving = false
                            Toast.makeText(
                                context,
                                "Profile updated successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener {
                            isSaving = false
                            Toast.makeText(
                                context,
                                "Could not save changes.Please try again",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save Changes")
            }
        }
        OutlinedButton(
            onClick = { onLogoutClick()},
            modifier = Modifier.fillMaxWidth(),
            colors= ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("LogOut")
        }
    }
}







