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
                                                    modifier = Modifier.width(70.dp).height(32.dp)
                                                        .padding(start = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = CoffeeGreen
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                                ) {
                                                    Text("Review", fontSize = 11.sp)
                                                }

                                                if (status.equals("Verified", ignoreCase = true) || status.equals("Milled & Settled", ignoreCase = true)) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    OutlinedButton(
                                                        onClick = {
                                                            selectedSubmissionForMilling = item
                                                            millGradeAA = ""
                                                            millGradeAB = ""
                                                            millGradePB = ""
                                                            millGradeC = ""
                                                        },
                                                        modifier = Modifier.width(55.dp).height(32.dp),
                                                        contentPadding = PaddingValues(horizontal = 2.dp)
                                                    ) {
                                                        Text("Mill", fontSize = 11.sp, color = CoffeeBrown)
                                                    }
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
                                            text = "Factory / Buyer Quality Grade",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CoffeeBrown
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = qualityGradeInput,
                                            onValueChange = { qualityGradeInput = it },
                                            label = { Text("Quality Grade (e.g. AA, AB, PB)") },
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
                                                        if (cooperativeId != null) {
                                                            db.collection("announcements").add(
                                                                hashMapOf(
                                                                    "cooperative_id" to cooperativeId,
                                                                    "title" to "Supply Volume Accepted - Bring to Cooperative",
                                                                    "message" to "Your coffee supply volume request has been accepted by the cooperative. Please bring your coffee delivery to the cooperative office for physical check.",
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
                                                val gradeInput = qualityGradeInput.ifBlank { "AA" }
                                                val updateData = mutableMapOf<String, Any>(
                                                    "status" to "Verified",
                                                    "verified_quantity_kg" to verifiedQty,
                                                    "quality_status" to "Passed",
                                                    "grade" to gradeInput,
                                                    "reviewed_by" to userName,
                                                    "reviewed_at" to System.currentTimeMillis()
                                                )

                                                db.collection("coffee_supply_submissions")
                                                    .document(submissionId)
                                                    .update(updateData)
                                                    .addOnSuccessListener {
                                                        if (cooperativeId != null) {
                                                            db.collection("announcements").add(
                                                                hashMapOf(
                                                                    "cooperative_id" to cooperativeId,
                                                                    "title" to "Coffee Supply Gate Verified!",
                                                                    "message" to "Your coffee delivery has been physically received and verified at the cooperative gate (Grade: $gradeInput).",
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
                                                                // Automatically post announcement for rejection & collection notice
                                                                if (cooperativeId != null) {
                                                                  db.collection("announcements").add(
                                                                      hashMapOf(
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

                                            val totalPayout = (aaKg * pAA) + (abKg * pAB) + (pbKg * pPB) + (cKg * pC)

                                            val millingReport = mapOf(
                                                "grade_aa_kg" to aaKg,
                                                "grade_ab_kg" to abKg,
                                                "grade_pb_kg" to pbKg,
                                                "grade_c_kg" to cKg,
                                                "total_payout_kes" to totalPayout,
                                                "milled_at" to System.currentTimeMillis()
                                            )

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
                        if(marketManagementTab!=2){
                            Button(
                                onClick={
                                    if(marketManagementTab==0){
                                        showAddFactoryDialog=true
                                    } else {
                                        showAddOfferDialog=true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoffeeGreen
                                ),
                                shape=RoundedCornerShape(12.dp)
                            ){
                                Text(if(marketManagementTab==0) "Add Factory" else "Add Offer")
                            }
                        } else if(marketManagementTab==2) {
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
                                                    text = "Offered: KES ${link.offeredPricePerKg}/kg |Net Return: KES ${link.netReturnPerKg}/kg",
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
                                                                    // Auto-close / remove matching market offer
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
                                                "grade" to offerGrade,"offered_price_per_kg" to (offerPricePerKg.toDoubleOrNull() ?: 0.0),
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



