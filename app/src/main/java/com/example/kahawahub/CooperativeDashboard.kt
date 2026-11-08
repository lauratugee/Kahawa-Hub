package com.example.kahawahub

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val CoffeeCream = Color(0xFFF7F3E9)
private val CoffeeBrown = Color(0xFF684832)
private val CoffeeGreen = Color(0xFF245640)

@Composable
fun  CooperativeDashboard(
    selectedTab: Int,
    onTabSelected:(Int) -> Unit,
    onLogoutClick: () -> Unit

) {
    val context = LocalContext.current

    val userName = remember { mutableStateOf("Cooperative Society") }
    val announcementTitle = remember { mutableStateOf("") }
    val announcementMessage = remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                        ?: document.getString("full_name")
                        ?: document.getString("fullName")
                        ?: currentUser.displayName
                        ?: currentUser.email?.substringBefore("@")
                        ?: ""
                    if (name.isNotBlank()) {
                        userName.value = name
                    } else {
                        db.collection("cooperatives")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { coopDoc ->
                                userName.value = coopDoc.getString("name")
                                    ?: currentUser.displayName
                                    ?: currentUser.email?.substringBefore("@")
                                    ?: "Cooperative Society"
                            }
                    }
                }
        }
    }

    Scaffold(
        containerColor = CoffeeCream,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                val tabs = listOf(
                    "Home",
                    "Members",
                    "Announcements",
                    "Prices",
                    "Profile"
                )
                val symbols = listOf("🏠", "🧑🏽‍🌾", "📃", "💰", "👤")

                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        icon = {
                            Text(
                                text = symbols[index],
                                fontSize = 15.sp
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CoffeeBrown,
                            selectedTextColor = CoffeeBrown,
                            indicatorColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    Text(
                        text = "Welcome, ${userName.value}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoffeeBrown
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your cooperative dashboard overview",
                        color = CoffeeBrown.copy(alpha = 0.8f)
                    )
                }

                1 -> {
                    Text(
                        text = "Members",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoffeeBrown
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Registered farmers will appear here",
                        color = CoffeeBrown.copy(alpha = 0.8f)
                    )
                }

                2 -> {
                    Text(
                        text = "Announcements",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoffeeBrown
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = announcementTitle.value,
                        onValueChange = { announcementTitle.value = it },
                        label = { Text("Announcement Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = announcementMessage.value,
                        onValueChange = { announcementMessage.value = it },
                        label = { Text("Announcement Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid

                            if (userId != null &&
                                announcementTitle.value.isNotBlank() &&
                                announcementMessage.value.isNotBlank()
                            ) {
                                val announcement = hashMapOf(
                                    "title" to announcementTitle.value,
                                    "message" to announcementMessage.value,
                                    "posted_by" to userId,
                                    "cooperative_id" to userId,
                                    "date_posted" to System.currentTimeMillis(),
                                    "target_audience" to "Farmers"
                                )
                                FirebaseFirestore.getInstance()
                                    .collection("announcements")
                                    .add(announcement)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Announcement posted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        announcementTitle.value = ""
                                        announcementMessage.value = ""
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(
                                            context,
                                            "Failed to post announcement: ${exception.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please fill in all fields or log in",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoffeeGreen
                        )
                    ) {
                        Text("Post Announcement")
                    }
                }

                3 -> {
                    Text(
                        text = "Prices",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoffeeBrown
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Manage coffee cherry prices here",
                        color = CoffeeBrown.copy(alpha = 0.8f)
                    )
                }

                4 -> {
                    Text(
                        text = "Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoffeeBrown
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userName.value,
                        fontSize = 16.sp,
                        color = CoffeeBrown.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Logout")
                    }
                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showLogoutDialog = false
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
                                        showLogoutDialog = false
                                        onLogoutClick()
                                    }
                                ) {
                                    Text("Logout", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showLogoutDialog = false }
                                ) {
                                    Text("Cancel")
                                }
                            },
                            containerColor = CoffeeCream
                        )
                    }
                }
            }
        }
    }
}
