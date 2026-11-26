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
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.rpc.Status

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
data class MarketLinkItem(
    val id: String="",
    val cooperativeId: String="",
    val farmerId: String="",
    val farmerName: String="",
    val buyerName: String="",
    val coffeeType: String="",
    val grade: String="",
    val offeredPricePerKg: Double = 0.0,
    val netReturnPerKg : Double = 0.0,
    val location: String="",
    val status: String="Pending",
    val dateRequested : Long=0L
)

@Composable
fun CooperativeDashboard(
    selectedTab: Int,
    onTabSelected:(Int) -> Unit,
    onLogoutClick: () -> Unit

) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    // Lets button handlers start coroutines (needed for the geocoding calls, which are suspend functions)
    val scope = rememberCoroutineScope()
    val cooperativeId = FirebaseAuth.getInstance().currentUser?.uid

    var userName by remember { mutableStateOf("Cooperative Society") }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var announcements by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var systemBroadcasts by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showAdminNotificationsDialog by remember { mutableStateOf(false) }
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
    var qualityGradeInput by remember { mutableStateOf("AA") }
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

    var marketLinks by remember { mutableStateOf<List<MarketLinkItem>>(emptyList()) }
    var isLinksLoading by remember { mutableStateOf(true) }

    var selectedSubmissionForMilling by remember { mutableStateOf<Map<String, Any>?>(null) }
    var millGradeAA by remember { mutableStateOf("") }
    var millGradeAB by remember { mutableStateOf("") }
    var millGradePB by remember { mutableStateOf("") }
    var millGradeC by remember { mutableStateOf("") }
    var pricePerAA by remember { mutableStateOf("130.0") }
    var pricePerAB by remember { mutableStateOf("110.0") }
    var pricePerPB by remember { mutableStateOf("95.0") }
    var pricePerC by remember { mutableStateOf("80.0") }

    var coopName by remember { mutableStateOf("Cooperative Society") }
    var coopLocation by remember { mutableStateOf("Location not specified") }
    var coopDescription by remember { mutableStateOf("No description provided.") }
    var coopServices by remember { mutableStateOf("No services listed.") }
    var coopMembership by remember { mutableStateOf("No membership info provided.") }
    var coopContact by remember { mutableStateOf("No contact info provided.") }
    var showEditCoopDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editServices by remember { mutableStateOf("") }
    var editMembership by remember { mutableStateOf("") }
    var editContact by remember { mutableStateOf("") }

    LaunchedEffect(cooperativeId) {
        if (cooperativeId != null) {
            db.collection("cooperatives")
                .document(cooperativeId)
                .addSnapshotListener { document, _ ->
                    if (document != null && document.exists()) {
                        coopName = document.getString("name") ?: document.getString("cooperative_name") ?: "Cooperative Society"
                        coopLocation = document.getString("location") ?: "Location not specified"
                        coopDescription = document.getString("description") ?: "No description provided."

                        val rawServices = document.get("services")
                        coopServices = when (rawServices) {
                            is List<*> -> rawServices.joinToString("\n• ", prefix = "• ")
                            is String -> rawServices
                            else -> "No services listed."
                        }

                        coopMembership = document.getString("membership_info") ?: document.getString("membership_requirements") ?: "No membership info provided."

                        val phone = document.getString("contact_number") ?: document.getString("phone") ?: ""
                        val email = document.getString("email") ?: ""
                        coopContact = "Phone: $phone\nEmail: $email"
                    }
                }
        }
    }



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
    // Listen to cooperative's own announcements (editable/deletable)
    DisposableEffect(cooperativeId) {
        if (cooperativeId == null) {
            announcements = emptyList()
            onDispose { }
        } else {
            val listener = db.collection("announcements")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Announcements", "Error fetching announcements: ${error.message}")
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
            onDispose { listener.remove() }
        }
    }

    // Listen to system-wide admin broadcasts for the notification bell icon
    DisposableEffect(Unit) {
        val listener = db.collection("announcements")
            .whereEqualTo("is_system_broadcast", true)
            .addSnapshotListener { snapshot, _ ->
                systemBroadcasts = snapshot?.documents?.map { document ->
                    mapOf(
                        "id" to document.id,
                        "title" to (document.getString("title") ?: ""),
                        "message" to (document.getString("message") ?: ""),
                        "date_posted" to (document.getLong("date_posted") ?: 0L)
                    )
                } ?: emptyList()
            }
        onDispose { listener.remove() }
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
            val listener = db.collection("factory_and_buyers")
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

    // Listen to factories and buyers for this cooperative in real-time
    DisposableEffect(cooperativeId) {
        val listener = if (cooperativeId == null) {
            factoriesAndBuyers = emptyList()
            isFactoriesLoading = false
            null
        } else {
            db.collection("factories_and_buyers")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            "Factory/Buyers",
                            "Error fetching factory/buyers: ${error.message}"
                        )
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
        }
        onDispose {
            listener?.remove()
        }
    }
    DisposableEffect(cooperativeId) {
        val listener=if (cooperativeId==null){
            marketLinks=emptyList()
            isLinksLoading=false
            null
        } else {
            db.collection("market_links")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MarketLinks", "Error fetching market links: ${error.message}")
                        isLinksLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot !=null){
                        marketLinks=snapshot.documents.map { document ->
                            MarketLinkItem(
                                id = document.id,
                                cooperativeId = document.getString("cooperative_id") ?: "",
                                farmerId = document.getString("farmer_id") ?: "",
                                farmerName = document.getString("farmer_name") ?: "",
                                buyerName = document.getString("buyer_name") ?: "",
                                coffeeType = document.getString("coffee_type") ?: "",
                                grade = document.getString("grade") ?: "",
                                offeredPricePerKg = document.getDouble("offered_price_per_kg") ?: 0.0,
                                netReturnPerKg = document.getDouble("net_return_per_kg") ?: 0.0,
                                location = document.getString("location") ?: "",
                                status = document.getString("status") ?: "Pending",
                                dateRequested = document.getLong("date_requested") ?: 0L
                            )
                        }.sortedBy { it.dateRequested }
                    }
                    isLinksLoading=false
                }
        }
        onDispose {
            listener?.remove()
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = coopName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoffeeBrown
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 $coopLocation",
                                    fontSize = 14.sp,
                                    color = CoffeeGreen
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showAdminNotificationsDialog = true }) {
                                    Box {
                                        Text("🔔", fontSize = 22.sp)
                                        if (systemBroadcasts.isNotEmpty()) {
                                            Badge(modifier = Modifier.align(Alignment.TopEnd)) {
                                                Text("${systemBroadcasts.size}")
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        editName = coopName
                                        editLocation = coopLocation
                                        editDescription = coopDescription
                                        editServices = coopServices
                                        editMembership = coopMembership
                                        editContact = coopContact
                                        showEditCoopDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("Edit Details", maxLines = 1, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "About Us",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = coopDescription,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 18.dp),
                            color = Color.LightGray
                        )

                        Text(
                            text = "Our Services",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoffeeBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = coopServices,
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
                            text = coopMembership,
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
                            text = coopContact,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }

                    if (showEditCoopDialog) {
                        AlertDialog(
                            onDismissRequest = { showEditCoopDialog = false },
                            title = {
                                Text(
                                    text = "Edit Cooperative Details",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoffeeBrown
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("Cooperative Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = editLocation,
                                        onValueChange = { editLocation = it },
                                        label = { Text("Location / Region") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = editDescription,
                                        onValueChange = { editDescription = it },
                                        label = { Text("About Us (Description)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = editServices,
                                        onValueChange = { editServices = it },
                                        label = { Text("Our Services") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = editMembership,
                                        onValueChange = { editMembership = it },
                                        label = { Text("Membership Information") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = editContact,
                                        onValueChange = { editContact = it },
                                        label = { Text("Contact Details") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (cooperativeId != null) {
                                            val updates = mapOf(
                                                "name" to editName,
                                                "location" to editLocation,
                                                "description" to editDescription,
                                                "services" to editServices,
                                                "membership_info" to editMembership,
                                                "contact_info" to editContact
                                            )
                                            db.collection("cooperatives")
                                                .document(cooperativeId)
                                                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Cooperative details updated!", Toast.LENGTH_SHORT).show()
                                                    showEditCoopDialog = false
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Failed to update: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Save Changes")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditCoopDialog = false }) {
                                    Text("Cancel")
                                }
                            }
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
                                FilterChip(
                                    selected=marketManagementTab==2,
                                    onClick = { marketManagementTab=2 },
                                    label = { Text("Requests (${marketLinks.size})") },
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
                            // Replaced horizontal scroll table with clean vertical mobile cards
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredSubmissions) { item ->
                                    val farmerId = item["farmer_id"] as? String ?: ""
                                    val memberMatch = members.find { it["user_id"] == farmerId || it["farmer_id"] == farmerId }
                                    
                                    val rawName = item["farmer_name"] as? String ?: ""
                                    val farmerName = if (rawName.isNotBlank() && !rawName.equals("Farmer", ignoreCase = true) && !rawName.equals("Unknown Farmer", ignoreCase = true)) {
                                        rawName
                                    } else {
                                        (memberMatch?.get("name") as? String)?.takeIf { it.isNotBlank() } ?: "Farmer"
                                    }

                                    val rawPhone = memberMatch?.get("phone_number") as? String ?: ""
                                    val phone = if (rawPhone.isNotBlank() && rawPhone != "Number not provided") rawPhone else "N/A"

                                    val itemCounty = item["county"] as? String ?: ""
                                    val itemSubCounty = item["sub_county"] as? String ?: ""
                                    val memberCounty = memberMatch?.get("county") as? String ?: ""
                                    val memberSubCounty = memberMatch?.get("sub_county") as? String ?: ""

                                    val county = itemCounty.ifBlank { memberCounty }
                                    val subCounty = itemSubCounty.ifBlank { memberSubCounty }

                                    val locationStr = if (subCounty.isNotBlank() || county.isNotBlank()) {
                                        listOf(subCounty, county).filter { it.isNotBlank() }.joinToString(", ")
                                    } else {
                                        "N/A"
                                    }

                                    val qtyKg = item["quantity_kg"] as? Long ?: 0L
                                    val verifiedQuantity = item["verified_quantity_kg"] as? Long ?: 0L
                                    val status = item["status"] as? String ?: "Pending"

                                    val (statusBg, statusFg) = when (status.lowercase()) {
                                        "verified" -> Color(0xFFC8E6C9) to CoffeeGreen
                                        "under review" -> Color(0xFFBBDEFB) to CoffeeBlue
                                        "rejected" -> Color(0xFFFFCDD2) to CoffeeRed
                                        else -> Color(0xFFFFE0B2) to CoffeeOrange
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = farmerName,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CoffeeBrown
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = statusBg
                                                ) {
                                                    Text(
                                                        text = status,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = statusFg,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Text(text = "📱 Contact: $phone | 📍 Location: $locationStr", fontSize = 13.sp, color = Color.DarkGray)
                                            Text(text = "☕ Coffee Type: ${item["coffee_type"] ?: ""} | ⚖️ Quantity: ${qtyKg} kg${if (verifiedQuantity > 0) " (Ver: ${verifiedQuantity} kg)" else ""}", fontSize = 13.sp, color = Color.DarkGray)

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        selectedSubmittedForReview = item
                                                        verifiedQuantityInput = (if (verifiedQuantity > 0) verifiedQuantity else qtyKg).toString()
                                                        rejectionReasonInput = item["rejection_reason"] as? String ?: ""
                                                    },
                                                    modifier = Modifier.weight(1f).height(36.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeGreen),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("Review", fontSize = 12.sp)
                                                }

                                                if (status.equals("Verified", ignoreCase = true) || status.equals("Milled & Settled", ignoreCase = true)) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            selectedSubmissionForMilling = item
                                                            millGradeAA = ""
                                                            millGradeAB = ""
                                                            millGradePB = ""
                                                            millGradeC = ""
                                                        },
                                                        modifier = Modifier.weight(1f).height(36.dp),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Text("Mill Report", fontSize = 12.sp, color = CoffeeBrown)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (selectedSubmittedForReview != null) {
                            val currentItem = selectedSubmittedForReview!!
                            val submissionId = currentItem["id"] as? String ?: ""
                            val farmerId = currentItem["farmer_id"] as? String ?: ""
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
                                                val updateData = mutableMapOf<String, Any>(
                                                    "status" to "Accepted - Bring to Coop",
                                                    "reviewed_by" to userName,
                                                    "reviewed_at" to System.currentTimeMillis()
                                                )

                                                db.collection("coffee_supply_submissions")
                                                    .document(submissionId)
                                                    .update(updateData)
                                                    .addOnSuccessListener {
                                                        // Send targeted personal notification to this specific farmer only
                                                        if (cooperativeId != null && farmerId.isNotBlank()) {
                                                            db.collection("farmer_notifications").add(
                                                                hashMapOf(
                                                                    "farmer_id" to farmerId,
                                                                    "cooperative_id" to cooperativeId,
                                                                    "title" to "Supply Volume Accepted - Bring to Cooperative",
                                                                    "message" to "Your coffee supply volume request has been accepted. Please bring your coffee delivery to the cooperative office for physical check.",
                                                                    "date_posted" to System.currentTimeMillis()
                                                                )
                                                            )
                                                        }

                                                        Toast.makeText(
                                                            context,
                                                            "Volume accepted. Delivery requested.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        selectedSubmittedForReview = null
                                                    }
                                                    .addOnFailureListener { error ->
                                                        Toast.makeText(
                                                            context,
                                                            "Failed: ${error.localizedMessage}",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("1. Accept Volume & Request Delivery")
                                        }

                                        Button(
                                            onClick = {
                                                val verifiedQty =
                                                    verifiedQuantityInput.toLongOrNull()
                                                        ?: declaredQuantity
                                                val updateData = mutableMapOf<String, Any>(
                                                    "status" to "Verified",
                                                    "verified_quantity_kg" to verifiedQty,
                                                    "quality_status" to "Passed",
                                                    "reviewed_by" to userName,
                                                    "reviewed_at" to System.currentTimeMillis()
                                                )

                                                db.collection("coffee_supply_submissions")
                                                    .document(submissionId)
                                                    .update(updateData)
                                                    .addOnSuccessListener {
                                                        // Send targeted personal notification to this specific farmer only
                                                        if (cooperativeId != null && farmerId.isNotBlank()) {
                                                            db.collection("farmer_notifications").add(
                                                                hashMapOf(
                                                                    "farmer_id" to farmerId,
                                                                    "cooperative_id" to cooperativeId,
                                                                    "title" to "Coffee Supply Gate Verified!",
                                                                    "message" to "Your coffee delivery has been physically received and verified at the cooperative gate ($verifiedQty kg verified).",
                                                                    "date_posted" to System.currentTimeMillis()
                                                                )
                                                            )
                                                        }

                                                        Toast.makeText(
                                                            context,
                                                            "Gate Verified successfully!",
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
                                            Text("2. Gate Verified & Quality Passed")
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
                                                            "quality_status" to "Failed",
                                                            "rejection_reason" to rejectionReasonInput,
                                                            "reviewed_by" to userName,
                                                            "reviewed_at" to System.currentTimeMillis()
                                                        )

                                                        db.collection("coffee_supply_submissions")
                                                            .document(submissionId)
                                                            .update(updateData)
                                                            .addOnSuccessListener {
                                                                // Automatically post targeted personal notification for rejection & collection notice
                                                                if (cooperativeId != null && farmerId.isNotBlank()) {
                                                                  db.collection("farmer_notifications").add(
                                                                      hashMapOf(
                                                                          "farmer_id" to farmerId,
                                                                          "cooperative_id" to cooperativeId,
                                                                          "title" to "Coffee Supply Quality Check Failed",
                                                                          "message" to "Your coffee supply submission failed factory/buyer quality inspection. Reason: $rejectionReasonInput. Please collect your coffee from your specific cooperative office.",
                                                                          "date_posted" to System.currentTimeMillis()
                                                                      )
                                                                  )
                                                                }

                                                                Toast.makeText(
                                                                    context,
                                                                    "Marked as Rejected with collection notice",
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

                        if (selectedSubmissionForMilling != null) {
                            val millItem = selectedSubmissionForMilling!!
                            val millSubId = millItem["id"] as? String ?: ""

                            // ===== Transport deduction =====
                            // The final payout subtracts the cost of hauling the coffee
                            // farm -> cooperative -> factory. Distances use the same map points
                            // shown to farmers, so what the farmer saw is what gets charged.
                            // All of this state is keyed by millSubId so it resets for each submission.
                            val millFarmerId = millItem["farmer_id"] as? String ?: ""
                            // Charge transport on the verified weight, falling back to the declared weight
                            val deliveredKg: Long = (millItem["verified_quantity_kg"] as? Long)?.takeIf { it > 0 }
                                ?: (millItem["quantity_kg"] as? Long) ?: 0L
                            var selectedFactoryId by remember(millSubId) { mutableStateOf("") }  // "" = no factory leg
                            var rateInput by remember(millSubId) {                              // editable KES/kg/km
                                mutableStateOf(DistanceUtils.DEFAULT_RATE_KES_PER_KG_KM.toString())
                            }
                            var coopPt by remember(millSubId) { mutableStateOf<com.google.android.gms.maps.model.LatLng?>(null) }
                            var farmerToCoopKm by remember(millSubId) { mutableStateOf<Double?>(null) }   // leg 1
                            var coopToFactoryKm by remember(millSubId) { mutableStateOf<Double?>(null) }  // leg 2

                            // Leg 1: when the dialog opens, locate the cooperative and the farmer
                            // and measure the distance between them (null if either can't be located)
                            LaunchedEffect(millSubId) {
                                val cp = if (cooperativeId != null) LocationResolver.cooperativePoint(db, context, cooperativeId) else null
                                coopPt = cp
                                val fp = LocationResolver.farmerPoint(db, context, millFarmerId)
                                farmerToCoopKm = if (cp != null && fp != null) DistanceUtils.calculateDistanceKm(fp, cp) else null
                            }
                            // Leg 2: whenever a different factory is chosen (or the cooperative position
                            // arrives), measure the distance from the cooperative to that factory
                            LaunchedEffect(selectedFactoryId, coopPt) {
                                coopToFactoryKm = null // clear the old value while we recalculate
                                val cp = coopPt
                                if (selectedFactoryId.isBlank() || cp == null) return@LaunchedEffect
                                try {
                                    val d = db.collection("factories_and_buyers").document(selectedFactoryId).get().awaitResult()
                                    val lat = d.getDouble("latitude")
                                    val lng = d.getDouble("longitude")
                                    // Saved coordinates if the factory has them, otherwise geocode its location text
                                    val fPoint = if (lat != null && lng != null) com.google.android.gms.maps.model.LatLng(lat, lng)
                                    else LocationResolver.geocode(context, d.getString("location").orEmpty())
                                    coopToFactoryKm = fPoint?.let { DistanceUtils.calculateDistanceKm(cp, it) }
                                } catch (e: kotlinx.coroutines.CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    coopToFactoryKm = null
                                }
                            }
                            // Rate typed by the cooperative (falls back to the default if blank/invalid)
                            val haulageRate = rateInput.toDoubleOrNull() ?: DistanceUtils.DEFAULT_RATE_KES_PER_KG_KM
                            // Cost per kg = (leg 1 km + leg 2 km) x rate. A leg whose location is
                            // unknown counts as 0 km, so it is not charged.
                            val transportPerKg = DistanceUtils.transportCostPerKg(
                                (farmerToCoopKm ?: 0.0) + (coopToFactoryKm ?: 0.0), haulageRate
                            )
                            // Total deducted from this farmer = per-kg cost x kg delivered
                            val transportTotal = transportPerKg * deliveredKg
                            val farmerName = millItem["farmer_name"] as? String ?: "Farmer"

                            AlertDialog(
                                onDismissRequest = { selectedSubmissionForMilling = null },
                                title = {
                                    Text(
                                        text = "Factory Milling & Final Settlement",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoffeeBrown
                                    )
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = "Farmer: $farmerName", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CoffeeGreen)
                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text("Enter Milled Output Quantities (kg):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CoffeeBrown)

                                        OutlinedTextField(
                                            value = millGradeAA,
                                            onValueChange = { millGradeAA = it },
                                            label = { Text("Grade AA Output (kg)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = millGradeAB,
                                            onValueChange = { millGradeAB = it },
                                            label = { Text("Grade AB Output (kg)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = millGradePB,
                                            onValueChange = { millGradePB = it },
                                            label = { Text("Grade PB Output (kg)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = millGradeC,
                                            onValueChange = { millGradeC = it },
                                            label = { Text("Grade C Output (kg)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Grade Pricing (KES per kg):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CoffeeBrown)

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(value = pricePerAA, onValueChange = { pricePerAA = it }, label = { Text("AA Price") }, modifier = Modifier.weight(1f), singleLine = true)
                                            OutlinedTextField(value = pricePerAB, onValueChange = { pricePerAB = it }, label = { Text("AB Price") }, modifier = Modifier.weight(1f), singleLine = true)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(value = pricePerPB, onValueChange = { pricePerPB = it }, label = { Text("PB Price") }, modifier = Modifier.weight(1f), singleLine = true)
                                            OutlinedTextField(value = pricePerC, onValueChange = { pricePerC = it }, label = { Text("C Price") }, modifier = Modifier.weight(1f), singleLine = true)
                                        }

                                        // ----- Transport section of the payout dialog -----
                                        HorizontalDivider(color = Color.LightGray)
                                        Text("Transport deduction:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CoffeeBrown)
                                        Text("Delivery factory / buyer:", fontSize = 12.sp, color = Color.DarkGray)
                                        // One button per factory, plus a "no factory" choice first.
                                        // The chosen one is ticked and lightly highlighted.
                                        (listOf("" to "No factory leg") + factoriesAndBuyers.map { it.id to it.entityName }).forEach { (id, label) ->
                                            val selected = selectedFactoryId == id
                                            OutlinedButton(
                                                onClick = { selectedFactoryId = id },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (selected) CoffeeGreen.copy(alpha = 0.12f) else Color.Transparent
                                                )
                                            ) {
                                                Text((if (selected) "✔ " else "") + label, fontSize = 12.sp, color = CoffeeBrown)
                                            }
                                        }
                                        // Editable haulage rate so the cooperative can match its real cost
                                        OutlinedTextField(
                                            value = rateInput,
                                            onValueChange = { rateInput = it },
                                            label = { Text("Haulage rate (KES per kg per km)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        // Live breakdown: leg 1 km, leg 2 km, and the resulting cost
                                        Text(
                                            text = "Farm → cooperative: " + (farmerToCoopKm?.let { "%.1f km".format(it) } ?: "farm location unknown (not charged)") +
                                                "\nCooperative → factory: " + (when {
                                                    selectedFactoryId.isBlank() -> "not applicable"
                                                    coopToFactoryKm != null -> "%.1f km".format(coopToFactoryKm)
                                                    else -> "factory location unknown (not charged)"
                                                }) +
                                                "\nTransport: KES %.2f/kg × %d kg = KES %.2f".format(transportPerKg, deliveredKg, transportTotal),
                                            fontSize = 12.sp,
                                            color = CoffeeGreen
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            val aaKg = millGradeAA.toLongOrNull() ?: 0L
                                            val abKg = millGradeAB.toLongOrNull() ?: 0L
                                            val pbKg = millGradePB.toLongOrNull() ?: 0L
                                            val cKg = millGradeC.toLongOrNull() ?: 0L

                                            val pAA = pricePerAA.toDoubleOrNull() ?: 130.0
                                            val pAB = pricePerAB.toDoubleOrNull() ?: 110.0
                                            val pPB = pricePerPB.toDoubleOrNull() ?: 95.0
                                            val pC = pricePerC.toDoubleOrNull() ?: 80.0

                                            // Gross = value of the milled coffee (kg x price for each grade)
                                            val grossPayout = (aaKg * pAA) + (abKg * pAB) + (pbKg * pPB) + (cKg * pC)
                                            // Net payout = milled value minus the farm → cooperative → factory haulage.
                                            // Floored at 0 so a large transport bill can never make the payout negative.
                                            val totalPayout = maxOf(0.0, grossPayout - transportTotal)
                                            // Name of the chosen factory, saved for the record ("" if none chosen)
                                            val factoryName = factoriesAndBuyers.firstOrNull { it.id == selectedFactoryId }?.entityName ?: ""

                                            val millingReport = mapOf(
                                                "grade_aa_kg" to aaKg,
                                                "grade_ab_kg" to abKg,
                                                "grade_pb_kg" to pbKg,
                                                "grade_c_kg" to cKg,
                                                // Transport breakdown stored with the milling report so the
                                                // payout can be audited later (all saved in the same document)
                                                "gross_payout_kes" to grossPayout,       // before transport
                                                "transport_cost_kes" to transportTotal,
                                                "transport_cost_per_kg" to transportPerKg,
                                                "transport_rate_per_kg_km" to haulageRate,
                                                "delivered_kg" to deliveredKg,
                                                "farmer_to_coop_km" to (farmerToCoopKm ?: 0.0),
                                                "coop_to_factory_km" to (coopToFactoryKm ?: 0.0),
                                                "factory_id" to selectedFactoryId,
                                                "factory_name" to factoryName,
                                                "total_payout_kes" to totalPayout,       // net, after transport
                                                "milled_at" to System.currentTimeMillis()
                                            )

                                            val farmerId = millItem["farmer_id"] as? String ?: ""
                                            db.collection("coffee_supply_submissions")
                                                .document(millSubId)
                                                .update(
                                                    mapOf(
                                                        "factory_milling_report" to millingReport,
                                                        "final_payout_amount" to totalPayout,
                                                        "status" to "Milled & Settled"
                                                    )
                                                )
                                                .addOnSuccessListener {
                                                    // Send targeted personal notification to this specific farmer only
                                                    if (cooperativeId != null && farmerId.isNotBlank()) {
                                                        db.collection("farmer_notifications").add(
                                                            hashMapOf(
                                                                "farmer_id" to farmerId,
                                                                "cooperative_id" to cooperativeId,
                                                                "title" to "Factory Milling & Final Payout Ready!",
                                                                "message" to "Your coffee has been milled and settled. Final Payout: KES %.2f (milled value KES %.2f less KES %.2f transport). Check your supply history for details.".format(totalPayout, grossPayout, transportTotal),
                                                                "date_posted" to System.currentTimeMillis()
                                                            )
                                                        )
                                                    }

                                                    Toast.makeText(context, "Final Settlement Payout Generated: KES %.2f".format(totalPayout), Toast.LENGTH_LONG).show()
                                                    selectedSubmissionForMilling = null
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Calculate & Generate Payout")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { selectedSubmissionForMilling = null }) {
                                        Text("Cancel")
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
                                    text = "Manage factories, buyers, and linkage requests",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
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
                                label = { Text("Factories & Buyers") },
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
                            FilterChip(
                                selected = marketManagementTab == 2,
                                onClick = {
                                    marketManagementTab = 2
                                },
                                label = { Text("Requests (${marketLinks.size})") },
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
                        } else if (marketManagementTab == 1) {
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
                        } else if (marketManagementTab == 2) {
                            if (isLinksLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = CoffeeGreen)
                                }
                            } else if (marketLinks.isEmpty()) {
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
                                            text = "No linkage requests received yet",
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
                                    items(marketLinks) { link ->
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
                                                        text = "Farmer: ${link.farmerName}",
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = CoffeeBrown
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = when (link.status.lowercase()) {
                                                            "completed" -> Color(0xFFC8E6C9)
                                                            "pending" -> Color(0xFFBBDEFB)
                                                            else -> Color(0xFFFFE0B2)
                                                        }
                                                    ) {
                                                        Text(
                                                            text = link.status,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (link.status.lowercase()) {
                                                                "completed" -> CoffeeGreen
                                                                "processing" -> Color(0xFF1976D2)
                                                                else -> CoffeeOrange
                                                            },
                                                            modifier = Modifier.padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                            )
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = "Buyer: ${link.buyerName} | Coffee: ${link.coffeeType} (${link.grade})",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))

                                                Text(
                                                    text = "Offered: KES ${link.offeredPricePerKg}/kg | Net Return: KES ${link.netReturnPerKg}/kg",
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))

                                                Text(
                                                    text = "Location: ${link.location.ifBlank { "N/A" }}",
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            db.collection("market_links")
                                                                .document(link.id)
                                                                .update("status", "Processing")
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Marked as Processing",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(
                                                            "Process",
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                    Button(
                                                        onClick = {
                                                            db.collection("market_links")
                                                                .document(link.id)
                                                                .update("status", "Completed")
                                                                .addOnSuccessListener {
                                                                    if (cooperativeId != null) {
                                                                        db.collection("market_offers")
                                                                            .whereEqualTo("cooperative_id", cooperativeId)
                                                                            .whereEqualTo("buyer_name", link.buyerName)
                                                                            .get()
                                                                            .addOnSuccessListener { querySnapshot ->
                                                                                for (offerDoc in querySnapshot.documents) {
                                                                                    db.collection("market_offers")
                                                                                        .document(offerDoc.id)
                                                                                        .delete()
                                                                                }
                                                                            }
                                                                    }

                                                                    Toast.makeText(
                                                                        context,
                                                                        "Marked as Completed & Offer Auto-Closed",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = CoffeeGreen
                                                        ),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text(
                                                            "Complete",
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Add Button only for Factories and Offers tabs
                        if (marketManagementTab != 2) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (marketManagementTab == 0) {
                                        showAddFactoryDialog = true
                                    } else {
                                        showAddOfferDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoffeeGreen
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (marketManagementTab == 0) "Add Factory" else "Add Offer")
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
                                            // Copy the typed location now: the success handler below clears
                                            // the input fields, and we still need the text for geocoding
                                            val factoryLocationText = inputLocation
                                            db.collection("factories_and_buyers")
                                                .add(payload)
                                                .addOnSuccessListener { newFactoryDoc ->
                                                    // The factory is saved. Now place it on the map so transport
                                                    // distances can be worked out: geocode the typed location in the
                                                    // background and write latitude/longitude onto the new document.
                                                    scope.launch {
                                                        val point = LocationResolver.geocode(context, factoryLocationText)
                                                        if (point != null) {
                                                            newFactoryDoc.update(
                                                                mapOf(
                                                                    "latitude" to point.latitude,
                                                                    "longitude" to point.longitude
                                                                )
                                                            )
                                                        } else {
                                                            // Nothing found: the factory stays saved but has no map pin,
                                                            // and won't be charged in payouts until its location is fixed
                                                            Toast.makeText(
                                                                context,
                                                                "Couldn't place \"$factoryLocationText\" on the map. Try a nearby town or county.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
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
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Enter essential market offer details. Standard transport and quantity defaults will apply automatically.",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray
                                        )
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
                                            label = { Text("Coffee Type (e.g. Coffee Cherries)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = offerGrade,
                                            onValueChange = { offerGrade = it },
                                            label = { Text("Grade / Quality (e.g. AA)") },
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
                                                "coffee_type" to offerCoffeeType.ifBlank { "Coffee Cherries" },
                                                "grade" to offerGrade.ifBlank { "AA" },
                                                "offered_price_per_kg" to (offerPricePerKg.toDoubleOrNull() ?: 0.0),
                                                "min_quantity" to (offerMinQuantity.toIntOrNull() ?: 50),
                                                "max_quantity" to (offerMaxQuantity.toIntOrNull() ?: 5000),
                                                "transport_costs" to (offerTransportCosts.toDoubleOrNull() ?: 10.0),
                                                "other_costs" to (offerOtherCosts.toDoubleOrNull() ?: 5.0),
                                                "location" to offerLocation.ifBlank { "Local Region" },
                                                "validity_date" to offerValidityDate.ifBlank { "30 Days" },
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

    // System Administrator Notifications Dialog for Cooperative Bell Icon
    if (showAdminNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showAdminNotificationsDialog = false },
            title = {
                Text(
                    text = "🔔 System Administrator Notices",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoffeeBrown
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (systemBroadcasts.isEmpty()) {
                        Text(text = "No system broadcasts available.", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        systemBroadcasts
                            .sortedByDescending { it["date_posted"] as? Long ?: 0L }
                            .forEach { notice ->
                                val title = notice["title"] as? String ?: ""
                                val message = notice["message"] as? String ?: ""
                                val date = notice["date_posted"] as? Long ?: 0L

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3E9))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = title, fontWeight = FontWeight.Bold, color = CoffeeBrown, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = message, fontSize = 13.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(date)),
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAdminNotificationsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }
}



