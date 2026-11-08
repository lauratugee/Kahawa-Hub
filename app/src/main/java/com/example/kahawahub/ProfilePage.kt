package com.example.kahawahub

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue

private val ProfileCream = Color(0xFFF7F3E9)
private val ProfileBrown = Color(0xFF684832)
private val ProfileGreen = Color(0xFF245640)
private val ProfileMuted = Color(0xFF806F60)
private val ProfileRed = Color(0xFFF44336)

@Composable
fun ProfilePage(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
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
    var isEditing by remember { mutableStateOf(false) }
    var showLeaveConfirmation by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser?.uid) {
        val userId = currentUser?.uid

        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                fullName = document.getString("name") ?: ""
                phoneNumber = document.getString("phone_number") ?: ""
                email = document.getString("email") ?: currentUser.email.orEmpty()
                userType = document.getString("user_type") ?: ""
                notificationsEnabled= document.getBoolean("notifications_enabled") ?: true
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
    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showLeaveConfirmation = false
            },
            title = {
                Text("Leave Cooperative")
            },
            text = {
                Text("Are you sure you want to leave this cooperative?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val userId = currentUser?.uid

                        if (userId != null) {
                            db.collection("farmers")
                                .document(userId)
                                .update(
                                    "cooperative_id",
                                    FieldValue.delete()
                                )
                                .addOnSuccessListener {
                                    cooperativeName = ""
                                    cooperativeLocation = ""
                                    showLeaveConfirmation = false

                                    Toast.makeText(
                                        context,
                                        "You have left the cooperative",
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
                    }
                ) {
                    Text("Leave", color = ProfileRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmation = false }) {
                    Text("Cancel")
                }
            },
            containerColor = ProfileCream
        )
    }


    if (showLogoutConfirmation){
        AlertDialog(
            onDismissRequest = {
                showLogoutConfirmation=false
            },
            title = {
                Text("Logout")
            },
            text = {
                Text("Are you sure you want to logout?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmation=false
                        onLogoutClick()
                    }
                ) {
                    Text("Logout",
                        color = ProfileRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel")
                }
            },
            containerColor = ProfileCream

        )
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileCream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "My Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ProfileBrown
        )

        Text(
            text = "Manage your personal information",
            fontSize = 14.sp,
            color = ProfileMuted
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ProfileGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .background(
                            color = Color.White,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        fontSize = 42.sp
                    )
                }
                Text(
                    text = fullName.ifBlank { "Farmer" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileBrown
                )
                Text(
                    text = userType.ifBlank { "User" },
                    fontSize = 14.sp,
                    color = ProfileMuted
                )
                if (userType.equals("Farmer", ignoreCase = true)) {
                    Surface(
                        color = ProfileGreen,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = cooperativeName.ifBlank {
                                "No cooperative joined"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            )
                        )
                    }
                }
            }

            ProfileSection(title = "Personal information") {
                if (isEditing) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                } else {
                    ProfileInfoRow(
                        emoji = "👤",
                        label = "Full name",
                        value = fullName.ifBlank { "Not provided" }
                    )
                    HorizontalDivider(color = Color.White)

                    ProfileInfoRow(
                        emoji = "💌",
                        label = "Email address",
                        value = email.ifBlank { "Not provided" }
                    )
                    HorizontalDivider(color = Color(0xFFEAA3D9))

                    ProfileInfoRow(
                        emoji = "📱",
                        label = "Phone number",
                        value = phoneNumber.ifBlank { "Not provided" }
                    )
                }
                if (isEditing) {
                    Button(
                        onClick = {
                            val userId = currentUser?.uid

                            if (userId == null) {
                                Toast.makeText(
                                    context,
                                    "Please log in again",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (
                                fullName.isBlank() || phoneNumber.isBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Name and phone number are required",
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
                                        fullName = fullName.trim()
                                        phoneNumber = phoneNumber.trim()
                                        isSaving = false
                                        isEditing = false

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
                                            "Could not save changes.Try again",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save changes")
                        }
                    }
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                } else {
                    OutlinedButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ProfileGreen
                        )
                    ) {
                        Text("Edit personal info")
                    }
                }
            }
            if (userType.equals("Farmer", ignoreCase = true)) {
                ProfileSection(title = "Cooperative membership") {

                    if (cooperativeName.isNotBlank()) {

                        ProfileInfoRow(
                            emoji = "🫱🏽‍🫲🏾",
                            label = "Cooperative",
                            value = cooperativeName
                        )
                        if (cooperativeLocation.isNotBlank()) {
                            HorizontalDivider(color = Color(0xFFEAE3D9))

                            ProfileInfoRow(
                                emoji = "📍",
                                label = "Location",
                                value = cooperativeLocation
                            )
                        }
                        OutlinedButton(
                            onClick = { showLeaveConfirmation = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ProfileRed
                            )
                        ) {
                            Text("Leave cooperative")
                        }
                    } else {
                        Text(
                            text = "You haven't joined a cooperative yet",
                            color = ProfileMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            ProfileSection(title = "Privacy settings") {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔔",
                        fontSize = 22.sp
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {
                        Text(
                            text = "Notifications",
                            color = ProfileBrown,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Receive updates and notifications",
                            color = ProfileMuted,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { newValue ->

                            val userId = currentUser?.uid

                            if (userId != null) {
                                db.collection("users")
                                    .document(userId)
                                    .update(
                                        "notifications_enabled", newValue
                                    )
                                    .addOnSuccessListener {
                                        notificationsEnabled = newValue

                                        Toast.makeText(
                                            context,
                                            "Notifications preferences updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Could not update notification preference",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    )
                }
            }


            Button(
                onClick = { showLogoutConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileRed
                )
            ) {
                Text(
                    text = "Logout",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ProfileInfoRow(
    emoji: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = ProfileMuted
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = ProfileBrown,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title:String,
    content: @Composable ColumnScope. () -> Unit
) {

    Column(
        modifier= Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Text(
            text=title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color=ProfileBrown
        )
        content()
    }
}
