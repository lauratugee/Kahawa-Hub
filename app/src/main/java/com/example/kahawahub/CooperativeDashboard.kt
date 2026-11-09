package com.example.kahawahub

import android.widget.Toast
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
private val CoffeeRed= Color(0xFFD32F2F)

@Composable
fun CooperativeDashboard(
    selectedTab: Int,
    onTabSelected:(Int) -> Unit,
    onLogoutClick: () -> Unit

) {
    val context = LocalContext.current
    val db=FirebaseFirestore.getInstance()
    val cooperativeId=FirebaseAuth.getInstance().currentUser?.uid

    var userName by remember { mutableStateOf("Cooperative Society") }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var announcements by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var editingAnnouncementId by remember { mutableStateOf<String?>(null) }
    var showAnnouncementMenu by remember { mutableStateOf<String?>(null) }
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

    DisposableEffect(Unit) {
        val cooperativeId = FirebaseAuth.getInstance().currentUser?.uid

        if (cooperativeId == null) {
            onDispose { }
        } else {
            val listener = FirebaseFirestore.getInstance()
                .collection("farmers")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e("Members", "Error fetching farmers: ${error.message}")
                        return@addSnapshotListener
                    }
                    members = emptyList()
                    snapshot?.documents?.forEach { document ->

                        val userId = document.getString("user_id")

                        if (!userId.isNullOrBlank()) {
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener { userDoc ->

                                    if (userDoc.exists()) {
                                        val memberDetails = mapOf(
                                            "farmer_id" to (document.getString("farmer_id")
                                                ?: document.id),
                                            "user_id" to userId,
                                            "name" to (userDoc.getString("name")
                                                ?: "Unkown User"),
                                            "phone_number" to (userDoc.getString("phone_number")
                                                ?: "Number not provided"),
                                            "sub_county" to (userDoc.getString("sub_county")
                                                ?: ""),
                                            "county" to (userDoc.getString("county") ?: "")
                                        )
                                        members = members.filterNot {
                                            it["user_id"] == userId
                                        } + memberDetails
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "Members",
                                        "Error fetching user details: ${exception.message}"
                                    )
                                }
                        }
                    }
                }
            onDispose {
                listener.remove()
            }
        }
    }
    DisposableEffect(cooperativeId) {

        if (cooperativeId != null) {
            announcements=emptyList()
            onDispose {  }
        } else {

            val listener = db.collection("announcements")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e(
                            "Announcements",
                            "Error fetching announcements: ${error.message}")
                        return@addSnapshotListener
                    }
                    announcements = snapshot?.documents?.map { document ->
                        mapOf(
                            "id" to document.id,
                            "title" to (document.getString("title") ?: ""),
                            "message" to (document.getString("message") ?: ""),
                            "date_posted" to (document.getLong("date_posted") ?: 0L)
                        )
                    } ?: emptyList()
                }
            onDispose {
                listener.remove()


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

                        Text("Members will appear here")
                    }
                }

                2 -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ){
                        Text(
                            text = "Announcements",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))


                        Text(
                            text=if(editingAnnouncementId!=null)
                                "Create Announcement"
                            else "Edit Announcement",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(12.dp))

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
                                val userId = cooperativeId

                                if (userId != null &&
                                    announcementTitle.isNotBlank() &&
                                    announcementMessage.isNotBlank()
                                ) {
                                    if (editingAnnouncementId != null) {
                                        db.collection("announcements")
                                            .document(editingAnnouncementId!!)
                                            .update(
                                                mapOf(
                                                    "title" to announcementTitle,
                                                    "message" to announcementMessage,
                                                    "date_updated" to System.currentTimeMillis()
                                                )
                                            )
                                            .addOnSuccessListener {
                                                announcementTitle = ""
                                                announcementMessage = ""
                                                editingAnnouncementId = null

                                                Toast.makeText(
                                                    context,
                                                    "Announcement updated successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(
                                                    context,
                                                    "Failed to update announcement: ${exception.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                    } else {
                                        val announcementData = hashMapOf(
                                            "title" to announcementTitle,
                                            "message" to announcementMessage,
                                            "posted_by" to userId,
                                            "cooperative_id" to userId,
                                            "date_posted" to System.currentTimeMillis(),
                                            "target_audience" to "Farmers"
                                        )

                                        db.collection("announcements")
                                            .add(announcementData)
                                            .addOnSuccessListener {
                                                announcementTitle = ""
                                                announcementMessage = ""

                                                Toast.makeText(
                                                    context,
                                                    "Announcement posted successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(
                                                    context,
                                                    "Failed to post announcement: ${exception.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
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
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoffeeGreen
                            )
                        ) {
                            Text(
                                if (editingAnnouncementId != null) "Save Changes" else "Post Announcement"
                            )
                        }
                        if (editingAnnouncementId != null) {
                            TextButton(
                                onClick = {
                                    announcementTitle = ""
                                    announcementMessage = ""
                                    editingAnnouncementId = null
                                }
                            ) {
                                Text("Cancel Editing", color = CoffeeBrown)
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color = Color.LightGray
                        )
                        Text(
                            text="My Announcements",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color=CoffeeBrown
                        )
                        Spacer(modifier=Modifier.height(12.dp))

                        if (announcements.isEmpty()) {
                            Text(
                                text = "No announcements yet",
                                color = Color.DarkGray,
                                fontSize = 14.sp
                            )
                        } else {
                            announcements
                                .sortedByDescending {
                                    it["date_posted"] as Long ?: 0L
                                }
                                .forEach { announcement ->

                                    val announcementId=announcement["id"] as String ?: ""

                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(bottom=12.dp),
                                        shape=RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ){
                                        Column(
                                            modifier=Modifier.padding(16.dp)
                                        ){
                                            Text(
                                                text=announcement["title"] as String ?: "",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CoffeeBrown
                                            )
                                            Spacer(modifier=Modifier.height(8.dp))

                                            Text(
                                                text=announcement["message"] as String ?: "",
                                                fontSize = 14.sp,
                                                color = Color.DarkGray
                                            )
                                            Spacer(modifier=Modifier.height(12.dp))

                                            Row(
                                                modifier=Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ){
                                                TextButton(
                                                    onClick = {
                                                        announcementTitle =
                                                            announcement["title"] as String ?: ""
                                                        announcementMessage =
                                                            announcement["message"] as String ?: ""
                                                        editingAnnouncementId = announcementId
                                                    }
                                                ){
                                                    Text(
                                                        text = "Edit",
                                                        color = CoffeeGreen
                                                    )
                                                }
                                                TextButton(
                                                    onClick = {
                                                        showAnnouncementMenu = announcementId
                                                    }
                                                ){
                                                    Text(
                                                        text = "Delete",
                                                        color = CoffeeRed
                                                    )
                                                }
                                            }
                                            if(showAnnouncementMenu==announcementId){
                                                AlertDialog(
                                                    onDismissRequest = {
                                                        showAnnouncementMenu = null
                                                    },
                                                    title = {
                                                        Text("Delete Announcement")
                                                    },
                                                    text = {
                                                        Text("Are you sure you want to delete this announcement?")
                                                    },
                                                    confirmButton = {
                                                        TextButton(
                                                            onClick = {
                                                                db.collection("announcements")
                                                                    .document(announcementId)
                                                                    .delete()
                                                                    .addOnSuccessListener {
                                                                        showAnnouncementMenu = null

                                                                        Toast.makeText(
                                                                            context,
                                                                            "Announcement deleted successfully",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                    .addOnFailureListener {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Failed to delete announcement",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                            }
                                                        ){
                                                            Text(
                                                                text = "Delete",
                                                                color = CoffeeRed
                                                            )
                                                        }
                                                    },
                                                    dismissButton = {
                                                        TextButton(
                                                            onClick = {
                                                                showAnnouncementMenu = null
                                                            }
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




