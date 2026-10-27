package com.example.kahawahub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.mutableStateListOf

private val FarmerCream = Color(0xFFF7F3E9)
private val FarmerBrown = Color(0xFF684832)
private val FarmerGreen = Color(0xFF245640)

private val PoppinsFamily = FontFamily(
    Font(R.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold)
)

data class FarmerAnnouncement(
    val title: String= "",
    val message: String= "",
    val date: String= ""
)

@Composable
fun FarmerDashboard(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onFindCooperativeClick: () -> Unit,
    onViewCoffeePricesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAnnouncementsClick: () -> Unit
) {
    var userName by remember { mutableStateOf("Farmer") }

    val announcements = remember {
        mutableStateListOf<FarmerAnnouncement>()
    }
    var announcementsLoading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: "Farmer"
                }
        }
    }
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            db.collection("farmers")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val cooperativeId = document.getString("cooperative_id")

                    if (cooperativeId.isNullOrBlank()) {
                        db.collection("announcements")
                            .whereEqualTo("cooperative_id", cooperativeId)
                            .get()
                            .addOnSuccessListener { documents ->

                                announcements.clear()

                                documents.documents.forEach { document ->
                                    announcements.add(
                                        FarmerAnnouncement(
                                            title = document.getString("title") ?: "",
                                            message = document.getString("message") ?: "",
                                            date = document.getString("date_posted") ?: ""
                                        )
                                    )
                                }
                                announcementsLoading = false
                            }
                            .addOnFailureListener {
                                announcementsLoading = false
                            }
                    } else {
                        announcementsLoading = false
                    }
                }
                .addOnFailureListener {
                    announcementsLoading = false
                }
        } else {
            announcementsLoading = false
        }
    }




    Scaffold(
        containerColor = FarmerCream,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0)  },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        onTabSelected(1)
                    },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Prices") },
                    label = { Text("Prices") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        onTabSelected(2)
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Cooperatives"
                        )
                    },
                    label = { Text("Cooperatives") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        onTabSelected(3)
                    },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab ) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FarmerCream)
                        .padding(innerPadding)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = "KAHAWA HUB",
                        fontFamily = PoppinsFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FarmerBrown
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Welcome, $userName",
                        fontFamily = PoppinsFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = FarmerBrown
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Your Coffee. Your Market.",
                        fontFamily = PoppinsFamily,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = FarmerGreen
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .background(
                                FarmerGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pic5),
                            contentDescription = "Background Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.FillWidth,
                            alignment = Alignment.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Cooperative Announcements",
                        fontSize = 20.sp,
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Medium,
                        color = FarmerBrown
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        announcementsLoading -> {
                            Text(
                                text = "Loading announcements...",
                                fontFamily = PoppinsFamily,
                                fontSize = 14.sp,
                                color = FarmerBrown
                            )
                        }

                        announcements.isEmpty() -> {
                            Text(
                                text = "No announcements available",
                                fontFamily = PoppinsFamily,
                                fontSize = 14.sp,
                                color = FarmerBrown
                            )
                        }

                        else -> {
                            announcements.forEach { announcement ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = FarmerCream
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = announcement.title,
                                            fontFamily = PoppinsFamily,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = FarmerGreen
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = announcement.message,
                                            fontFamily = PoppinsFamily,
                                            fontSize = 14.sp,
                                            color = FarmerBrown
                                        )
                                        if (announcement.date.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = announcement.date,
                                                fontFamily = PoppinsFamily,
                                                fontSize = 12.sp,
                                                color = FarmerBrown
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CoffeeMarketPricesPage(
                        onBackClick = {
                            onTabSelected(0)
                        }
                    )
                }
            }
            2 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    FindCooperativePage(
                        onBackClick = {
                            onTabSelected(0)
                        }
                    )
                }
            }
            3 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ProfilePage(
                        onBackClick = {
                            onTabSelected(0)
                        },
                        onLogoutClick = {
                            FirebaseAuth.getInstance().signOut()
                            onTabSelected
                        }
                    )
                }
                }
            }
    }
}

