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
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.input.TextFieldValue
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val CoffeeCream = Color(0xFFF7F3E9)
private val CoffeeBrown = Color(0xFF684832)
private val CoffeeGreen = Color(0xFF245640)

@Composable
fun CooperativeDashboard(
    selectedTab: Int,
    onTabSelected:(Int) -> Unit,
    onLogoutClick: () -> Unit

) {
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Cooperative Society") }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var members by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }


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
                        userName = name
                    } else {
                        db.collection("cooperatives")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { coopDoc ->
                                userName= coopDoc.getString("name")
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
                val symbols = listOf("🏠","👥","📃","💰","👤")

                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index)
                        },

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
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    Column(
                        modifier=Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ){
                        Box(
                            modifier= Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pic4),
                                contentDescription = "cooperative society header",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = 18.dp, y = 35.dp),
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 5.dp
                            ) {
                                Box(
                                    modifier = Modifier.size(70.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("☕", fontSize = 34.sp)
                                }
                            }
                        }
                        Spacer(modifier=Modifier.height(48.dp))

                        Text(
                            text = "Welcome, ${userName}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color=CoffeeBrown
                        )
                        Spacer(modifier=Modifier.height(6.dp))

                        Text(
                            text="Cooperative location",
                            fontSize = 14.sp,
                            color=CoffeeGreen
                        )
                        Spacer(modifier=Modifier.height(24.dp))

                        Text(
                            text="About Us",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color=CoffeeBrown
                        )
                        Spacer(modifier=Modifier.height(8.dp))

                        Text(
                            text="Description of the cooperative",
                            fontSize = 14.sp,
                            color=Color.DarkGray,
                            lineHeight = 22.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color= Color.Gray
                        )
                        Text(
                            text="Our Services",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color=CoffeeBrown
                        )
                        Spacer(modifier=Modifier.height(8.dp))

                        Text(
                            text="Services offered will be displayed here",
                            fontSize = 14.sp,
                            color=Color.DarkGray,
                            lineHeight = 22.sp
                        )
                        HorizontalDivider(
                            modifier=Modifier.padding(vertical=18.dp),
                            color= Color.LightGray
                        )
                        Text(
                            text="Membership Information",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color=CoffeeBrown
                        )
                        Spacer(modifier=Modifier.height(8.dp))

                        Text(
                            text="Information about the cooperative members will be displayed here",
                            fontSize = 14.sp,
                            color=Color.DarkGray,
                            lineHeight = 22.sp
                        )
                        HorizontalDivider(
                            modifier=Modifier.padding(vertical=18.dp),
                            color= Color.LightGray
                        )
                        Text(
                            text="Contact Details",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color=CoffeeBrown
                        )
                        Spacer(modifier=Modifier.height(8.dp))

                        Text(
                            text="Contact information for the cooperative will be displayed here",
                            fontSize = 14.sp,
                            color=Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }

                }

                1 -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Members",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search for members") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Memers will appear here")
                    }
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
                        value = announcementTitle,
                        onValueChange = { announcementTitle = it },
                        label = { Text("Announcement Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape=RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = announcementMessage,
                        onValueChange = { announcementMessage = it },
                        label = { Text("Announcement Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape=RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid

                            if (userId != null &&
                                announcementTitle.isNotBlank() &&
                                announcementMessage.isNotBlank()
                            ) {
                                val announcement = hashMapOf(
                                    "title" to announcementTitle,
                                    "message" to announcementMessage,
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

                                        announcementTitle = ""
                                        announcementMessage = ""
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Failed to post announcement: &{it.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please fill in all fields",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    },
                        modifier = Modifier.fillMaxWidth(),
                        shape=RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoffeeGreen
                        )
                    ){
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
                    Text(
                        text = "Manage coffee cherry prices here",
                        modifier = Modifier.padding(top = 12.dp),
                        color=Color.DarkGray
                    )
                }

                4 -> {
                    Text(
                        text = "Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoffeeBrown
                    )
                    Spacer(modifier=Modifier.height(12.dp))


                    Text(
                        text = userName,
                        modifier = Modifier.padding(top = 12.dp),
                        color=Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
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
                                    onClick = onLogoutClick
                                ) {
                                    Text("Logout")
                                }

                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showLogoutDialog = false }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}




