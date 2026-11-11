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
import androidx.compose.foundation.horizontalScroll
import android.util.Log
import androidx.compose.foundation.background
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val CoffeeCream = Color(0xFFF7F3E9)
private val CoffeeBrown = Color(0xFF684832)
private val CoffeeGreen = Color(0xFF245640)
private val CoffeeRed= Color(0xFFD32F2F)
private val CoffeeBlue=Color (0xFF1976D2)
private val CoffeeOrange=Color(0xFFE65100)


data class FactoryBuyerItem(
    val id: String="",
    val cooperativeId: String="",
    val entityName: String="",
    val entityType: String="Commercial buyer/Factory/Miller",
    val location: String="",
    val contactInfo: String="",
    val acceptedCoffeeTypes: String="",
    val qualityRequirements: String="",
    val dateAdded : Long = 0L
)
data class MarketOfferItem(
    val id: String="",
    val cooperativeId: String="",
    val buyerName: String="",
    val coffeeType: String="",
    val grade: String="",
    val offeredPricePerKg: Double= 0.0,
    val minQuantity: Int=0,
    val maxQuantity: Int=0,
    val transportCosts : Double=0.0,
    val otherCosts: Double=0.0,
    val location: String="",
    val validityDat: String="",
    val dateAdded: Long=0L,
    val addedBy: String="",
)

@Composable
fun CooperativeDashboard(
    selectedTab: Int,
    onTabSelected:(Int) -> Unit,
    onLogoutClick: () -> Unit

) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val cooperativeId = FirebaseAuth.getInstance().currentUser?.uid

    var userName by remember { mutableStateOf("Cooperative Society") }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var announcements by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var editingAnnouncementId by remember { mutableStateOf<String?>(null) }
    var showAnnouncementMenu by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var members by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var cherriesPrice by remember { mutableStateOf("") }
    var mbuniPrice by remember { mutableStateOf("") }
    var pulpedPrice by remember { mutableStateOf("") }
    var submissions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedSubmittedForReview by remember { mutableStateOf<Map<String, Any>?>(null) }
    var verifiedQuantityInput by remember { mutableStateOf("") }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var factoriesAndBuyers by remember { mutableStateOf<List<FactoryBuyerItem>>(emptyList()) }
    var isFactoriesLoading by remember { mutableStateOf(true) }
    var showAddFactoryDialog by remember { mutableStateOf(false) }

    var inputEntityName by remember { mutableStateOf("") }
    var inputEntityType by remember { mutableStateOf("Commercial Buyer/Factory/Miller") }
    var inputLocation by remember { mutableStateOf("") }
    var inputContactInfo by remember { mutableStateOf("") }
    var inputAcceptedCoffeeTypes by remember { mutableStateOf("") }
    var inputQualityRequirements by remember { mutableStateOf("") }

    var marketOffers by remember { mutableStateOf<List<MarketOfferItem>>(emptyList()) }
    var isOffersLoading by remember { mutableStateOf(true) }
    var showAddOfferDialog by remember { mutableStateOf(false) }
    var marketManagementTab by remember { mutableStateOf(0) }

    var offerBuyerName by remember { mutableStateOf("") }
    var offerCoffeeType by remember { mutableStateOf("") }
    var offerGrade by remember { mutableStateOf("") }
    var offerPricePerKg by remember { mutableStateOf("") }
    var offerMinQuantity by remember { mutableStateOf("") }
    var offerMaxQuantity by remember { mutableStateOf("") }
    var offerTransportCosts by remember { mutableStateOf("") }
    var offerOtherCosts by remember { mutableStateOf("") }
    var offerLocation by remember { mutableStateOf("") }
    var offerValidityDate by remember { mutableStateOf("") }



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
                                userName = coopDoc.getString("name")
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

        if (cooperativeId == null) {
            announcements = emptyList()
            onDispose { }
        } else {

            val listener = db.collection("announcements")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e(
                            "Announcements",
                            "Error fetching announcements: ${error.message}"
                        )
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
    DisposableEffect(cooperativeId) {
        if (cooperativeId == null) {
            submissions = emptyList()
            onDispose { }
        } else {
            val listener = db.collection("coffee_supply_submissions")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("KahawaSupply", "Error fetching submissions ${error.message}",)
                        return@addSnapshotListener
                    }
                    submissions = snapshot?.documents?.map { document ->
                        mapOf(
                            "id" to document.id,
                            "farmer_id" to (document.getString("farmer_id") ?: ""),
                            "farmer_name" to (document.getString("farmer_name") ?: ""),
                            "coffee_type" to (document.getString("coffee_type") ?: ""),
                            "quantity_kg" to (document.getLong("quantity_kg") ?: 0L),
                            "verified_quantity_kg" to (document.getLong("verified_quantity_kg")
                                ?: 0L),
                            "rejection_reason" to (document.getString("rejection_reason") ?: ""),
                            "status" to (document.getString("status") ?: ""),
                            "date_submitted" to (document.getLong("date_submitted") ?: 0L),
                            "county" to (document.getString("county") ?: ""),
                            "sub_county" to (document.getString("sub_county") ?: "")
                        )
                    } ?: emptyList()
                }
            onDispose {
                listener.remove()

            }
        }
    }
    DisposableEffect(cooperativeId) {
        if (cooperativeId == null) {
            factoriesAndBuyers = emptyList()
            isFactoriesLoading = false
            onDispose { }
        } else {
            val listener = db.collection("factory_buyers")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Factory/Buyers", "Error fetching factory/buyers: ${error.message}")
                        isFactoriesLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        factoriesAndBuyers = snapshot.documents.map { document ->
                            FactoryBuyerItem(
                                id = document.id,
                                cooperativeId = document.getString("cooperative_id") ?: "",
                                entityName = document.getString("entity_name") ?: "",
                                entityType = document.getString("entity_type")
                                    ?: "Commercial Buyer/Factory/Miller",
                                location = document.getString("location") ?: "",
                                contactInfo = document.getString("contact_info") ?: "",
                                acceptedCoffeeTypes = document.getString("accepted_coffee_types")
                                    ?: "",
                                qualityRequirements = document.getString("quality_requirements")
                                    ?: "",
                                dateAdded = document.getLong("date_added") ?: 0L
                            )
                        }.sortedByDescending { it.dateAdded }
                    }
                    isFactoriesLoading = false
                }
            onDispose {
                listener.remove()
            }
        }
    }
    DisposableEffect(cooperativeId) {
        if (cooperativeId == null) {
            marketOffers = emptyList()
            isOffersLoading = false
            onDispose { }
        } else {
            val listener = db.collection("market_offers")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MarketOffers", "Error fetching market offers: ${error.message}")
                        isOffersLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        marketOffers = snapshot.documents.map { document ->
                            MarketOfferItem(
                                id = document.id,
                                cooperativeId = document.getString("cooperative_id") ?: "",
                                buyerName = document.getString("buyer_name") ?: "",
                                coffeeType = document.getString("coffee_type") ?: "",
                                grade = document.getString("grade") ?: "",
                                offeredPricePerKg = (document.getDouble("offered_price_per_kg")
                                    ?: 0.0),
                                minQuantity = (document.getLong("min_quantity") ?: 0).toInt(),
                                maxQuantity = (document.getLong("max_quantity") ?: 0).toInt(),
                                transportCosts = (document.getDouble("transport_costs") ?: 0.0),
                                otherCosts = (document.getDouble("other_costs") ?: 0.0),
                                location = document.getString("location") ?: "",
                                validityDat = document.getString("validity_date") ?: "",
                                dateAdded = document.getLong("date_added") ?: 0L,
                                addedBy = document.getString("added_by") ?: ""
                            )
                        }.sortedByDescending { it.dateAdded }
                    }
                    isOffersLoading = false
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
                    "Market",
                    "Profile"
                )
                val symbols = listOf("🏠", "👥", "📃", "💰", "💹", "👤")

                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            onTabSelected(index)
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
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
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
                        Spacer(modifier = Modifier.height(48.dp))

                        Text(
                            text = "Welcome, ${userName}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Cooperative location",
                            fontSize = 14.sp,
                            color = CoffeeGreen
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "About Us",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Description of the cooperative",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color = Color.Gray
                        )
                        Text(
                            text = "Our Services",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Services offered will be displayed here",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color = Color.LightGray
                        )
                        Text(
                            text = "Membership Information",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Information about the cooperative members will be displayed here",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color = Color.LightGray
                        )
                        Text(
                            text = "Contact Details",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Contact information for the cooperative will be displayed here",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }

                }

                1 -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Member Supply Submissions",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Review, verify and manage coffee supply submissions",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search for members") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val statusFilters =
                            listOf("All", "Pending", "Under Review", "Verified", "Rejected")

                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusFilters.forEach { status ->
                                FilterChip(
                                    selected = selectedStatusFilter == status,
                                    onClick = { selectedStatusFilter = status },
                                    label = { Text(status) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoffeeGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        val filteredSubmissions = submissions.filter { sub ->
                            val farmerName = (sub["farmer_name"] as String) ?: ""
                            val farmerId = (sub["farmer_id"] as String) ?: ""
                            val memberMatch =
                                members.find { it["user_id"] == farmerId || it["farmer_id"] == farmerId }
                            val phone = memberMatch?.get("phone_number") as String ?: ""
                            val status = (sub["status"] as String) ?: "Pending"

                            val matchesSearch =
                                farmerName.contains(searchQuery, ignoreCase = true) ||
                                        phone.contains(searchQuery, ignoreCase = true)
                            val matchesStatus = selectedStatusFilter == "All" ||
                                    status.equals(selectedStatusFilter, ignoreCase = true)
                            matchesSearch && matchesStatus
                        }

                        if (filteredSubmissions.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(
                                    text = if (submissions.isEmpty()) "No submissions yet" else "No matching submissions",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .background(CoffeeBrown)
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Farmer Name",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(110.dp)
                                        )
                                        VerticalDivider(
                                            modifier = Modifier.height(20.dp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        )
                                        Text(
                                            "Contact Number",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(100.dp).padding(start = 6.dp)
                                        )
                                        VerticalDivider(
                                            modifier = Modifier.height(20.dp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        )
                                        Text(
                                            "Location",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(110.dp).padding(start = 6.dp)
                                        )
                                        VerticalDivider(
                                            modifier = Modifier.height(20.dp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        )
                                        Text(
                                            "Coffee Type",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(100.dp).padding(start = 6.dp)
                                        )
                                        VerticalDivider(
                                            modifier = Modifier.height(20.dp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        )
                                        Text(
                                            "Quantity",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(80.dp).padding(start = 6.dp)
                                        )
                                        VerticalDivider(
                                            modifier = Modifier.height(20.dp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        )
                                        Text(
                                            "Status",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(85.dp).padding(start = 6.dp)
                                        )
                                        VerticalDivider(
                                            modifier = Modifier.height(20.dp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        )
                                        Text(
                                            "Action",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(75.dp).padding(start = 6.dp)
                                        )
                                    }
                                    HorizontalDivider(color = Color.LightGray)

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredSubmissions) { item ->
                                            val farmerId = item["farmer_id"] as? String ?: ""
                                            val memberMatch =
                                                members.find { it["user_id"] == farmerId || it["farmer_id"] == farmerId }
                                            val phone =
                                                memberMatch?.get("phone_number") as? String ?: "N/A"
                                            val county = item["county"] as? String ?: ""
                                            val subCounty = item["sub_county"] as? String ?: ""
                                            val locationStr =
                                                if (subCounty.isNotBlank() || county.isNotBlank()) {
                                                    "(${subCounty.ifBlank { "N/A" }}, ${county.ifBlank { "N/A" }})"
                                                } else {
                                                    "N/A"
                                                }

                                            val qtyKg = item["quantity_kg"] as? Long ?: 0L
                                            val verifiedQuantity =
                                                item["verified_quantity_kg"] as? Long ?: 0L
                                            val status = item["status"] as? String ?: "Pending"

                                            val (statusBg, statusFg) = when (status.lowercase()) {
                                                "verified" -> Color(0xFFC8E6C9) to CoffeeGreen
                                                "under review" -> Color(0xFFBBDEFB) to CoffeeBlue
                                                "rejected" -> Color(0xFFFFCDD2) to CoffeeRed
                                                else -> Color(0xFFFFE0B2) to CoffeeOrange
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    item["farmer_name"] as? String ?: "Unknown",
                                                    modifier = Modifier.width(110.dp),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                )
                                                VerticalDivider(
                                                    modifier = Modifier.height(28.dp),
                                                    color = Color.LightGray.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    phone,
                                                    modifier = Modifier.width(100.dp)
                                                        .padding(start = 6.dp),
                                                    fontSize = 12.sp,
                                                    color = Color.DarkGray
                                                )
                                                VerticalDivider(
                                                    modifier = Modifier.height(28.dp),
                                                    color = Color.LightGray.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    locationStr,
                                                    modifier = Modifier.width(110.dp)
                                                        .padding(start = 6.dp),
                                                    fontSize = 12.sp,
                                                    color = Color.DarkGray
                                                )
                                                VerticalDivider(
                                                    modifier = Modifier.height(28.dp),
                                                    color = Color.LightGray.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    item["coffee_type"] as? String ?: "",
                                                    modifier = Modifier.width(100.dp)
                                                        .padding(start = 6.dp),
                                                    fontSize = 12.sp
                                                )
                                                VerticalDivider(
                                                    modifier = Modifier.height(28.dp),
                                                    color = Color.LightGray.copy(alpha = 0.5f)
                                                )
                                                Column(
                                                    modifier = Modifier.width(80.dp)
                                                        .padding(start = 6.dp)
                                                ) {
                                                    Text(
                                                        "${qtyKg} kg",
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 12.sp
                                                    )
                                                    if (status.equals(
                                                            "Verified",
                                                            ignoreCase = true
                                                        ) && verifiedQuantity > 0
                                                    ) {
                                                        Text(
                                                            "Ver: ${verifiedQuantity} kg",
                                                            fontSize = 10.sp,
                                                            color = CoffeeGreen
                                                        )
                                                    }
                                                }
                                                VerticalDivider(
                                                    modifier = Modifier.height(28.dp),
                                                    color = Color.LightGray.copy(alpha = 0.5f)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = statusBg,
                                                    modifier = Modifier.width(85.dp)
                                                        .padding(start = 6.dp)
                                                ) {
                                                    Text(
                                                        text = status,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = statusFg,
                                                        modifier = Modifier.padding(
                                                            horizontal = 6.dp,
                                                            vertical = 4.dp
                                                        )
                                                    )
                                                }
                                                VerticalDivider(
                                                    modifier = Modifier.height(28.dp),
                                                    color = Color.LightGray.copy(alpha = 0.5f)
                                                )
                                                Button(
                                                    onClick = {
                                                        selectedSubmittedForReview = item
                                                        verifiedQuantityInput =
                                                            (if (verifiedQuantity > 0) verifiedQuantity else qtyKg).toString()
                                                        rejectionReasonInput =
                                                            item["rejection_reason"] as? String
                                                                ?: ""
                                                    },
                                                    modifier = Modifier.width(75.dp).height(32.dp)
                                                        .padding(start = 6.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = CoffeeGreen
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                                ) {
                                                    Text("Review", fontSize = 11.sp)
                                                }
                                            }
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            }
                        }
                        if (selectedSubmittedForReview != null) {
                            val currentItem = selectedSubmittedForReview!!
                            val submissionId = currentItem["id"] as? String ?: ""
                            val declaredQuantity = currentItem["quantity_kg"] as? Long ?: 0L

                            AlertDialog(
                                onDismissRequest = { selectedSubmittedForReview = null },
                                title = {
                                    Text(
                                        text = "Review Coffee Supply",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoffeeBrown
                                    )
                                },
                                text = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Declared Quantity: $declaredQuantity kg",
                                            fontSize = 14.sp,
                                            color = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Verified Quantity (kg)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CoffeeBrown
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = verifiedQuantityInput,
                                            onValueChange = { newValue ->
                                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                                    verifiedQuantityInput = newValue
                                                }
                                            },
                                            label = { Text("Verified kg") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Rejection Reason (If rejecting)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CoffeeBrown
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = rejectionReasonInput,
                                            onValueChange = { rejectionReasonInput = it },
                                            label = { Text("Reason for rejection") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 2,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                },
                                confirmButton = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val verifiedQty =
                                                    verifiedQuantityInput.toLongOrNull()
                                                        ?: declaredQuantity
                                                val updateData = mutableMapOf<String, Any>(
                                                    "status" to "Verified",
                                                    "verified_quantity_kg" to verifiedQty,
                                                    "reviewed_by" to userName,
                                                    "reviewed_at" to System.currentTimeMillis()
                                                )

                                                db.collection("coffee_supply_submissions")
                                                    .document(submissionId)
                                                    .update(updateData)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "Submission verified successfully!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        selectedSubmittedForReview = null
                                                    }
                                                    .addOnFailureListener { error ->
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to verify: ${error.localizedMessage}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = CoffeeGreen),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("🟢 Verify")
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    val updateData = mutableMapOf<String, Any>(
                                                        "status" to "Under Review",
                                                        "reviewed_by" to userName,
                                                        "reviewed_at" to System.currentTimeMillis()
                                                    )

                                                    db.collection("coffee_supply_submissions")
                                                        .document(submissionId)
                                                        .update(updateData)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(
                                                                context,
                                                                "Marked as Under Review",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            selectedSubmittedForReview = null
                                                        }
                                                        .addOnFailureListener { error ->
                                                            Toast.makeText(
                                                                context,
                                                                "Failed to update: ${error.localizedMessage}",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("🔵 Under Review", fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    if (rejectionReasonInput.isBlank()) {
                                                        Toast.makeText(
                                                            context,
                                                            "Please enter a rejection reason first",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        val updateData = mutableMapOf<String, Any>(
                                                            "status" to "Rejected",
                                                            "rejection_reason" to rejectionReasonInput,
                                                            "reviewed_by" to userName,
                                                            "reviewed_at" to System.currentTimeMillis()
                                                        )

                                                        db.collection("coffee_supply_submissions")
                                                            .document(submissionId)
                                                            .update(updateData)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Submission rejected",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                selectedSubmittedForReview = null
                                                            }
                                                            .addOnFailureListener { error ->
                                                                Toast.makeText(
                                                                    context,
                                                                    "Failed to reject: ${error.localizedMessage}",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = CoffeeRed),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("🔴 Reject", fontSize = 12.sp)
                                            }
                                        }
                                        TextButton(
                                            onClick = {
                                                selectedSubmittedForReview = null
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Cancel",
                                                color = Color.Gray,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }


                2 -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Announcements",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))


                        Text(
                            text = if (editingAnnouncementId != null)
                                "Edit Announcement"
                            else "Create Announcement",
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
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = announcementMessage,
                            onValueChange = { announcementMessage = it },
                            label = { Text("Announcement Message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            shape = RoundedCornerShape(12.dp)
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
                            text = "My Announcements",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(12.dp))

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

                                    val announcementId = announcement["id"] as String ?: ""

                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = announcement["title"] as String ?: "",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CoffeeBrown
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            val datePosted =
                                                announcement["date_posted"] as? Long ?: 0L

                                            Text(
                                                text = if (datePosted > 0L) {
                                                    SimpleDateFormat(
                                                        "dd/MM/yyyy",
                                                        Locale.getDefault()
                                                    ).format(Date(datePosted))
                                                } else {
                                                    "Unknown Date"
                                                },
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))


                                            Text(
                                                text = announcement["message"] as String ?: "",
                                                fontSize = 14.sp,
                                                color = Color.DarkGray
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        announcementTitle =
                                                            announcement["title"] as String ?: ""
                                                        announcementMessage =
                                                            announcement["message"] as String ?: ""
                                                        editingAnnouncementId = announcementId
                                                    }
                                                ) {
                                                    Text(
                                                        text = "Edit",
                                                        color = CoffeeGreen
                                                    )
                                                }
                                                TextButton(
                                                    onClick = {
                                                        showAnnouncementMenu = announcementId
                                                    }
                                                ) {
                                                    Text(
                                                        text = "Delete",
                                                        color = CoffeeRed
                                                    )
                                                }
                                            }
                                            if (showAnnouncementMenu == announcementId) {
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
                                                        ) {
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

                3 -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Coffee Buying Prices",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val categories = listOf(
                            "Coffee Cherries" to "Fresh, raw cherries",
                            "Mbuni" to "Dried coffee cherries",
                            "Pulped Coffee" to "Pulpled and dried coffee"
                        )
                        categories.forEach { category ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = category.first,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoffeeBrown
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = category.second,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "KSH per kg",
                                        fontSize = 14.sp,
                                        color = CoffeeGreen
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = when (category.first) {
                                            "Coffee Cherries" -> cherriesPrice
                                            "Mbuni" -> mbuniPrice
                                            "Pulped Coffee" -> pulpedPrice
                                            else -> ""
                                        },
                                        onValueChange = { newValue ->
                                            when (category.first) {
                                                "Coffee Cherries" -> cherriesPrice = newValue
                                                "Mbuni" -> mbuniPrice = newValue
                                                "Pulped Coffee" -> pulpedPrice = newValue
                                            }
                                        },
                                        label = {
                                            Text("Price")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )


                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            val enteredPrice = when (category.first) {
                                                "Coffee Cherries" -> cherriesPrice
                                                "Mbuni" -> mbuniPrice
                                                "Pulped Coffee" -> pulpedPrice
                                                else -> ""
                                            }
                                            val priceValue = enteredPrice.toLongOrNull()

                                            if (cooperativeId == null) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to update price",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else if (priceValue == null || priceValue <= 0) {
                                                Toast.makeText(
                                                    context,
                                                    "Please enter a valid price",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val categoryKey = when (category.first) {
                                                    "Coffee Cherries" -> "cherries_price"
                                                    "Mbuni" -> "mbuni_price"
                                                    "Pulped Coffee" -> "pulped_price"
                                                    else -> ""
                                                }
                                                val priceDocument = db.collection("coffee_prices")
                                                    .document("${cooperativeId} - ${categoryKey}")


                                                priceDocument.get()
                                                    .addOnSuccessListener { document ->

                                                        val previousPrice =
                                                            document.getLong("price") ?: 0L

                                                        val priceData = hashMapOf(
                                                            "coffee_type" to category.first,
                                                            "price" to priceValue,
                                                            "previous_price" to previousPrice,
                                                            "currency" to "KES",
                                                            "price_unit" to "per kg",
                                                            "cooperative_name" to userName,
                                                            "cooperative_id" to cooperativeId,
                                                            "date_updated" to System.currentTimeMillis()
                                                        )
                                                        priceDocument.set(priceData)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(
                                                                    context,
                                                                    " ${category.first} Price updated successfully",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                            .addOnFailureListener { error ->
                                                                Toast.makeText(
                                                                    context,
                                                                    "Failed to update price: ${error.message}",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                    }
                                                    .addOnFailureListener { error ->
                                                        Toast.makeText(
                                                            context,
                                                            "Failed to fetch previous price: ${error.message}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CoffeeGreen
                                        )
                                    ) {
                                        Text("Update Price")
                                    }
                                }
                            }


                        }


                    }
                }

                4 -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Market Management",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoffeeBrown
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Manage factories and buyers",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Button(
                                onClick = {
                                    if (marketManagementTab == 0)
                                        showAddFactoryDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoffeeGreen
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (marketManagementTab == 0) "Add Factory" else "Add Offer")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = marketManagementTab == 0,
                                onClick = {
                                    marketManagementTab = 0
                                },
                                label = { Text("Factories and Buyers") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoffeeGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = marketManagementTab == 1,
                                onClick = {
                                    marketManagementTab = 1
                                },
                                label = { Text("Offers (${marketOffers.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoffeeGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (marketManagementTab == 0) {
                            if (isFactoriesLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = CoffeeGreen)
                                }
                            } else if (factoriesAndBuyers.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No factories/buyers found",
                                            fontSize = 14.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(factoriesAndBuyers) { factory ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = factory.entityName,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CoffeeBrown
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = if (factory.entityType.contains(
                                                                "Factory",
                                                                ignoreCase = true
                                                            )
                                                        ) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                                                    ) {
                                                        Text(
                                                            text = factory.entityType,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (factory.entityType.contains(
                                                                    "Factory",
                                                                    ignoreCase = true
                                                                )
                                                            ) CoffeeOrange else CoffeeGreen,
                                                            modifier = Modifier.padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                            )
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Location: ${factory.location.ifBlank { "N/A" }} | Contact: ${factory.contactInfo.ifBlank { "N/A" }}",
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Accepted Coffee: ${factory.acceptedCoffeeTypes.ifBlank { "All Types" }}",
                                                    fontSize = 13.sp,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Requirements: ${factory.qualityRequirements.ifBlank { "Standard Quality" }}",
                                                    fontSize = 13.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (isOffersLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = CoffeeGreen)
                                }
                            } else if (marketOffers.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No offers found",
                                            fontSize = 14.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(marketOffers) { offer ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = offer.buyerName,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CoffeeBrown
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = Color(0xFFE8F5E9)
                                                    ) {
                                                        Text(
                                                            text = "KES ${offer.offeredPricePerKg} / kg",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = CoffeeGreen,
                                                            modifier = Modifier.padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                            )
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = "Coffee: ${offer.coffeeType} (${offer.grade})",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Quantity Range: ${offer.minQuantity} kg - ${offer.maxQuantity} kg | Location: ${offer.location.ifBlank { "N/A" }}",
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Transport Cost: KES ${offer.transportCosts} | Other Costs: KES ${offer.otherCosts}",
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }



                        if (showAddFactoryDialog) {
                            AlertDialog(
                                onDismissRequest = { showAddFactoryDialog = false },
                                title = {
                                    Text(
                                        text = "Add New Factory or Buyer",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoffeeBrown
                                    )
                                },
                                text = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = inputEntityName,
                                            onValueChange = { inputEntityName = it },
                                            label = { Text("Name") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = inputEntityType,
                                            onValueChange = { inputEntityType = it },
                                            label = { Text("Type (Commercial Buyer / Factory)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = inputLocation,
                                            onValueChange = { inputLocation = it },
                                            label = { Text("Location") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = inputContactInfo,
                                            onValueChange = { inputContactInfo = it },
                                            label = { Text("Contact Info") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = inputAcceptedCoffeeTypes,
                                            onValueChange = { inputAcceptedCoffeeTypes = it },
                                            label = { Text("Accepted Coffee Types") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = inputQualityRequirements,
                                            onValueChange = { inputQualityRequirements = it },
                                            label = { Text("Quality Requirements") },
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 2,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            if (cooperativeId == null) {
                                                Toast.makeText(
                                                    context,
                                                    "Cooperative ID missing",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@Button
                                            }
                                            if (inputEntityName.isBlank()) {
                                                Toast.makeText(
                                                    context,
                                                    "Please enter entity name",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@Button
                                            }
                                            val payload = hashMapOf(
                                                "cooperative_id" to cooperativeId,
                                                "entity_name" to inputEntityName,
                                                "entity_type" to inputEntityType,
                                                "location" to inputLocation,
                                                "contact_info" to inputContactInfo,
                                                "accepted_coffee_types" to inputAcceptedCoffeeTypes,
                                                "quality_requirements" to inputQualityRequirements,
                                                "date_added" to System.currentTimeMillis(),
                                                "added_by" to userName
                                            )
                                            db.collection("factories_and_buyers")
                                                .add(payload)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Factory/Buyer added successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    showAddFactoryDialog = false

                                                    inputEntityName = ""
                                                    inputLocation = ""
                                                    inputContactInfo = ""
                                                    inputAcceptedCoffeeTypes = ""
                                                    inputQualityRequirements = ""
                                                }
                                                .addOnFailureListener { error ->
                                                    Toast.makeText(
                                                        context,
                                                        "Failed: ${error.localizedMessage}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Save")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showAddFactoryDialog = false }
                                    ) {
                                        Text("Cancel", color = Color.Gray)
                                    }
                                }
                            )
                        }
                        if (showAddOfferDialog) {
                            AlertDialog(
                                onDismissRequest = { showAddOfferDialog = false },
                                title = {
                                    Text(
                                        text = "Add New Offer",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoffeeBrown
                                    )
                                },
                                text = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = offerBuyerName,
                                            onValueChange = { offerBuyerName = it },
                                            label = { Text("Buyer/Mill Name") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = offerCoffeeType,
                                            onValueChange = { offerCoffeeType = it },
                                            label = { Text("Coffee Type") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = offerGrade,
                                            onValueChange = { offerGrade = it },
                                            label = { Text("Grade/ Quality") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = offerPricePerKg,
                                            onValueChange = { offerPricePerKg = it },
                                            label = { Text("Offered price per kg (KES)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = offerMinQuantity,
                                                onValueChange = { offerMinQuantity = it },
                                                label = { Text("Min Quantity") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            OutlinedTextField(
                                                value = offerMaxQuantity,
                                                onValueChange = { offerMaxQuantity = it },
                                                label = { Text("Max Quantity") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = offerTransportCosts,
                                                onValueChange = {
                                                    if (it.isEmpty() || it.all { ch -> ch.isDigit() || ch == '.' }) offerTransportCosts =
                                                        it
                                                },
                                                label = { Text("Transport") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            OutlinedTextField(
                                                value = offerOtherCosts,
                                                onValueChange = {
                                                    if (it.isEmpty() || it.all { ch -> ch.isDigit() || ch == '.' }) offerOtherCosts =
                                                        it
                                                },
                                                label = { Text("Other Costs") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }
                                        OutlinedTextField(
                                            value = offerLocation,
                                            onValueChange = { offerLocation = it },
                                            label = { Text("Location") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = offerValidityDate,
                                            onValueChange = { offerValidityDate = it },
                                            label = { Text("Validity") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            if (cooperativeId == null) {
                                                Toast.makeText(
                                                    context,
                                                    "Cooperative ID missing",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@Button
                                            }
                                            if (offerBuyerName.isBlank() || offerPricePerKg.isBlank()) {
                                                Toast.makeText(
                                                    context,
                                                    "Please fill in all fields",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@Button
                                            }
                                            val payload = hashMapOf(
                                                "cooperative_id" to cooperativeId,
                                                "buyer_name" to offerBuyerName,
                                                "coffee_type" to offerCoffeeType,
                                                "grade" to offerGrade,
                                                "offered_price_per_kg" to (offerPricePerKg.toDoubleOrNull() ?: 0.0),
                                                "min_quantity" to (offerMinQuantity.toIntOrNull() ?: 0),
                                                "max_quantity" to (offerMaxQuantity.toIntOrNull() ?: 0),
                                                "transport_costs" to (offerTransportCosts.toDoubleOrNull() ?: 0.0),
                                                "other_costs" to (offerOtherCosts.toDoubleOrNull() ?: 0.0),
                                                "location" to offerLocation,
                                                "validity_date" to offerValidityDate,
                                                "date_added" to System.currentTimeMillis()
                                            )
                                            db.collection("market_offers")
                                                .add(payload)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Offer added successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    showAddOfferDialog = false

                                                    offerBuyerName = ""
                                                    offerCoffeeType = ""
                                                    offerGrade = ""
                                                    offerPricePerKg = ""
                                                    offerMinQuantity = ""
                                                    offerMaxQuantity = ""
                                                    offerTransportCosts = ""
                                                    offerOtherCosts = ""
                                                    offerLocation = ""
                                                    offerValidityDate = ""
                                                }
                                                .addOnFailureListener { error ->
                                                    Toast.makeText(
                                                        context,
                                                        "Failed: ${error.localizedMessage}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Save")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showAddOfferDialog = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }

                5 -> {
                        CooperativeProfilePage(
                            onLogoutClick = onLogoutClick
                        )
                    }
                }
            }
        }
    }
    



