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

private val FarmerCream = Color(0xFFF7F3E9)
private val FarmerBrown = Color(0xFF684832)
private val FarmerGreen = Color(0xFF245640)

@Composable
fun FarmerDashboard(
    onFindCooperativeClick: () -> Unit,
    onViewCoffeePricesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAnnouncementsClick: () -> Unit
) {
    var userName by remember { mutableStateOf("Farmer") }
    var selectedTab by remember { mutableStateOf(0) }

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
    Scaffold(
        containerColor = FarmerCream,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onViewCoffeePricesClick()
                    },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Prices") },
                    label = { Text("Prices") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onFindCooperativeClick()
                    },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Cooperatives") },
                    label = { Text("Cooperatives") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onProfileClick()
                    },
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FarmerCream)
                    .padding(innerPadding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {

                Text(
                    text = "COFFEE MARKET",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmerBrown
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Welcome, $userName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FarmerBrown
                )
                Spacer(modifier = Modifier.height(20.dp))

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
                        painter = painterResource(id = R.drawable.pic6),
                        contentDescription = "Background Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Latest Announcements",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FarmerBrown
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Stay updated with news/information from your cooperative",
                    fontSize = 16.sp,
                    color = FarmerBrown
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAnnouncementsClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FarmerGreen)
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = "View Announcements",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
