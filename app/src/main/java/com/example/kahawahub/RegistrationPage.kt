package com.example.kahawahub

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

private val RegistrationCream = Color(0xFFF7F3E9)
private val RegistrationBrown = Color(0xFF684832)
private val RegistrationGreen = Color(0xFF245640)

@Composable
fun RegistrationPage(
    onLoginClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("") }
    var userTypeExpanded by remember { mutableStateOf(false) }
    var cooperativeName by remember { mutableStateOf("") }
    var cooperativeLocation by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RegistrationCream)
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "KAHAWA HUB",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = RegistrationBrown
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your account",
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            color = RegistrationBrown
        )
        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RegistrationGreen,
                unfocusedBorderColor = RegistrationBrown.copy(alpha = 0.5f),
                focusedLabelColor = RegistrationGreen,
                cursorColor = RegistrationGreen,
                focusedTextColor = RegistrationBrown,
                unfocusedTextColor = RegistrationBrown
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RegistrationGreen,
                unfocusedBorderColor = RegistrationBrown.copy(alpha = 0.5f),
                focusedLabelColor = RegistrationGreen,
                cursorColor = RegistrationGreen,
                focusedTextColor = RegistrationBrown,
                unfocusedTextColor = RegistrationBrown
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone No.") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RegistrationGreen,
                unfocusedBorderColor = RegistrationBrown.copy(alpha = 0.5f),
                focusedLabelColor = RegistrationGreen,
                cursorColor = RegistrationGreen,
                focusedTextColor = RegistrationBrown,
                unfocusedTextColor = RegistrationBrown
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it.trim() },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RegistrationGreen,
                unfocusedBorderColor = RegistrationBrown.copy(alpha = 0.5f),
                focusedLabelColor = RegistrationGreen,
                cursorColor = RegistrationGreen,
                focusedTextColor = RegistrationBrown,
                unfocusedTextColor = RegistrationBrown
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it.trim() },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RegistrationGreen,
                unfocusedBorderColor = RegistrationBrown.copy(alpha = 0.5f),
                focusedLabelColor = RegistrationGreen,
                cursorColor = RegistrationGreen,
                focusedTextColor = RegistrationBrown,
                unfocusedTextColor = RegistrationBrown
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { userTypeExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RegistrationCream,
                    contentColor = RegistrationBrown
                ),
                border = BorderStroke(
                    1.dp,
                    RegistrationBrown.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (userType.isEmpty()) "Select user type" else userType,
                    fontSize = 16.sp
                )
            }
            DropdownMenu(
                expanded = userTypeExpanded,
                onDismissRequest = { userTypeExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Farmer") },
                    onClick = {
                        userType = "Farmer"
                        userTypeExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cooperative Society") },
                    onClick = {
                        userType = "Cooperative Society"
                        userTypeExpanded = false
                    }
                )
            }
        }

        if (userType == "Cooperative Society") {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cooperativeName,
                onValueChange = { cooperativeName = it },
                label = { Text("Cooperative Name") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RegistrationGreen,
                    unfocusedBorderColor = RegistrationBrown.copy(alpha = 0.5f),
                    focusedLabelColor = RegistrationGreen,
                    cursorColor = RegistrationGreen,
                    focusedTextColor = RegistrationBrown,
                    unfocusedTextColor = RegistrationBrown
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = cooperativeLocation,
                onValueChange = { cooperativeLocation = it },
                label = { Text("Cooperative Location") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RegistrationGreen,
                    unfocusedBorderColor = RegistrationBrown.copy(alpha = 0.5f),
                    focusedLabelColor = RegistrationGreen,
                    cursorColor = RegistrationGreen,
                    focusedTextColor = RegistrationBrown,
                    unfocusedTextColor = RegistrationBrown
                )
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    Toast.makeText(
                        context,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (userType.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Please select a user type",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (
                    userType == "Cooperative Society" &&
                    (cooperativeName.isEmpty() || cooperativeLocation.isEmpty())
                ) {
                    Toast.makeText(
                        context,
                        "Please enter your cooperative name and location",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    auth.createUserWithEmailAndPassword(
                        email,
                        password
                    )
                        .addOnSuccessListener { result ->
                            val userId = result.user?.uid

                            if (userId != null) {
                                val userData = hashMapOf(
                                    "user_id" to userId,
                                    "name" to fullName,
                                    "email" to email,
                                    "phone_number" to phoneNumber,
                                    "user_type" to userType
                                )
                                db.collection("users")
                                    .document(userId)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        FirebaseMessaging.getInstance().token
                                            .addOnSuccessListener { token ->
                                                db.collection("users")
                                                    .document(userId)
                                                    .update("fcm_token", token)
                                                    .addOnSuccessListener {
                                                        Log.d(
                                                            "KahawaFCM",
                                                            "FCM token saved after registration"
                                                        )
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Log.e(
                                                            "KahawaFCM",
                                                            "Could not save FCM token after registration",
                                                            exception
                                                        )
                                                    }
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e(
                                                    "KahawaFCM",
                                                    "Could not retrieve FCM token after registration",
                                                    exception
                                                )
                                            }

                                        if (userType == "Cooperative Society") {
                                            val cooperativeData = hashMapOf(
                                                "cooperative_id" to userId,
                                                "name" to cooperativeName,
                                                "location" to cooperativeLocation,
                                                "contact_number" to phoneNumber,
                                                "email" to email,
                                                "created_at" to System.currentTimeMillis()
                                            )
                                            db.collection("cooperatives")
                                                .document(userId)
                                                .set(cooperativeData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Registration successful",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(
                                                        context,
                                                        "Cooperative database error: ${exception.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Registration successful",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
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
                                "Registration failed: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RegistrationGreen
            )
        ) {
            Text(
                text = "Register",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Already have an account?",
            color = RegistrationBrown,
            fontSize = 14.sp
        )

        TextButton(
            onClick = onLoginClick
        ) {
            Text(
                text = "Login",
                color = RegistrationGreen,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
