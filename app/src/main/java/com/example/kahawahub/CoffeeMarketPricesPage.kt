package com.example.kahawahub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

private val MarketCream = Color(0xFFF7F3E9)
private val MarketBrown = Color(0xFF684832)
private val MarketGreen = Color(0xFF245640)
private val MarketMuted = Color(0xFF81766C)
private val MarketRed = Color(0xFFB4483E)
private val MarketLightGreen = Color(0xFFC8E6C9)
private val MarketLightRed = Color(0xFFFFCDD2)

data class MarketPrice(
    val id: String = "",
    val coffeeType: String = "",
    val price: Long = 0L,
    val previousPrice: Long = 0L,
    val currency: String = "KES",
    val priceUnit: String = "per kg",
    val cooperativeName: String = "",
    val cooperativeId: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeMarketPricesPage(
    cooperativeId: String? = null,
    onBackClick: () -> Unit
){


    var prices by remember { mutableStateOf<List<MarketPrice>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }

    var searchText by remember { mutableStateOf("") }
    var selectedCoffeeType by remember { mutableStateOf("All types") }
    var selectedCooperative by remember { mutableStateOf("All cooperatives") }

    var showFilterMenu by remember { mutableStateOf(false) }
    var showCoffeeMenu by remember { mutableStateOf(false) }
    var showCooperativeMenu by remember { mutableStateOf(false) }
    var farmerMarketTab by remember { mutableStateOf(0) }

    var verifiedSubmissions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var marketOffers by remember { mutableStateOf<List<MarketOfferItem>>(emptyList()) }
    var recommendations by remember { mutableStateOf<List<MarketRecommendation>>(emptyList()) }
    var myLinkageRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid


    DisposableEffect(cooperativeId) {
        isLoading = true
        if (cooperativeId.isNullOrBlank()) {
            prices = emptyList()
            isLoading = false
            onDispose { }
        } else {
            val query = FirebaseFirestore.getInstance().collection("coffee_prices")
                .whereEqualTo("cooperative_id", cooperativeId)

            val listener = query.addSnapshotListener { documents, error ->
                if (error != null) {
                    loadError = true
                    isLoading = false
                    return@addSnapshotListener
                }

                if (documents != null) {
                    prices = documents.documents.map { document ->
                        MarketPrice(
                            id = document.id,
                            coffeeType = document.getString("coffee_type") ?: "",
                            price = document.getLong("price") ?: 0L,
                            previousPrice = document.getLong("previous_price") ?: 0L,
                            currency = document.getString("currency") ?: "KES",
                            priceUnit = document.getString("price_unit") ?: "per kg",
                            cooperativeName = document.getString("cooperative_name") ?: "",
                            cooperativeId = document.getString("cooperative_id") ?: ""
                        )
                    }
                    loadError = false
                    isLoading = false
                }
            }

            onDispose {
                listener.remove()
            }
        }
    }
    DisposableEffect(userId) {
        if(userId==null){
            onDispose {  }
        }else{
            val listener = FirebaseFirestore.getInstance()
                .collection("coffee_supply_submissions")
                .whereEqualTo("farmer_id", userId)
                .whereEqualTo("status", "Verified")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        verifiedSubmissions = snapshot.documents.map { document ->
                            document.data ?: emptyMap()
                        }
                    }
                }
            onDispose { listener.remove() }
        }
    }
    DisposableEffect(cooperativeId) {
        if (cooperativeId.isNullOrBlank()) {
            onDispose { }
        } else {
            val listener = FirebaseFirestore.getInstance()
                .collection("market_offers")
                .whereEqualTo("cooperative_id", cooperativeId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        marketOffers = snapshot.documents.map { document ->
                            MarketOfferItem(
                                id = document.id,
                                cooperativeId = document.getString("cooperative_id") ?: "",
                                buyerName = document.getString("buyer_name") ?: "",
                                coffeeType = document.getString("coffee_type") ?: "",
                                grade = document.getString("grade") ?: "",
                                offeredPricePerKg = document.getDouble("offered_price_per_kg")
                                    ?: 0.0,
                                minQuantity = document.getLong("min_quantity")?.toInt() ?: 0,
                                maxQuantity = document.getLong("max_quantity")?.toInt() ?: 0,
                                transportCosts = document.getDouble("transport_costs") ?: 0.0,
                                otherCosts = document.getDouble("other_costs") ?: 0.0,
                                location = document.getString("location") ?: "",
                                validityDat = document.getString("validity_date") ?: "",
                                dateAdded = document.getLong("date_added") ?: 0L,
                                addedBy = document.getString("added_by") ?: ""
                            )
                        }
                    }
                }
            onDispose { listener.remove() }
        }
    }
    // Listen to current farmer's active linkage requests
    DisposableEffect(userId) {
        if (userId.isNullOrBlank()) {
            onDispose { }
        } else {
            val listener = FirebaseFirestore.getInstance()
                .collection("market_links")
                .whereEqualTo("farmer_id", userId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        myLinkageRequests = snapshot.documents.map { doc ->
                            doc.data ?: emptyMap()
                        }
                    }
                }
            onDispose { listener.remove() }
        }
    }

    LaunchedEffect(verifiedSubmissions, marketOffers) {
        recommendations =
            MarketAnalyzer.analyzeAndRankOpportunities(verifiedSubmissions, marketOffers)
    }










    val coffeeTypes = prices
        .map { it.coffeeType }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }
        .sorted()

    val cooperatives = prices
        .map { marketPrice ->
            val label = marketPrice.cooperativeName.ifBlank {
                if (marketPrice.cooperativeId.isNotBlank()) {
                    "Cooperative ${marketPrice.cooperativeId.takeLast(5)}"
                } else {
                    "Cooperative (UNKNOWN)"
                }
            }
            marketPrice.cooperativeId to label
        }
        .distinctBy { it.first }
        .sortedBy { it.second.lowercase() }

    val filteredPrices = prices.filter { marketPrice ->
        val cooperativeLabel = marketPrice.cooperativeName.ifBlank {
            if (marketPrice.cooperativeId.isNotBlank()) {
                "Cooperative ${marketPrice.cooperativeId.takeLast(5)}"
            } else {
                "Cooperative (UNKNOWN)"
            }
        }
        val matchesSearch =
            marketPrice.coffeeType.contains(searchText, ignoreCase = true) ||
                    cooperativeLabel.contains(searchText, ignoreCase = true)

        val matchesCoffee =
            selectedCoffeeType == "All types" ||
                    marketPrice.coffeeType.equals(selectedCoffeeType, ignoreCase = true)

        val matchesCooperative =
            selectedCooperative == "All cooperatives" ||
                    marketPrice.cooperativeId == selectedCooperative

        matchesSearch && matchesCoffee && matchesCooperative
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coffee Market Prices", color = MarketBrown) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MarketBrown
                        )
                    }
                }
            )
        },
        containerColor = MarketCream
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = farmerMarketTab == 0,
                    onClick = { farmerMarketTab = 0 },
                    label = { Text("Market Prices") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MarketGreen,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = farmerMarketTab == 1,
                    onClick = { farmerMarketTab = 1 },
                    label = { Text("Recommendations (${recommendations.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MarketGreen,
                        selectedLabelColor = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (farmerMarketTab == 1) {
                if (recommendations.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🌱", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "No recommendations available yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MarketBrown
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Ensure your coffee supply has been verified",
                                    fontSize = 12.sp,
                                    color = MarketMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(recommendations) { rec ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
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
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = rec.buyerName,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MarketBrown
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Coffee: ${rec.coffeeType}  ${rec.grade}",
                                                    fontSize = 12.sp,
                                                    color = MarketGreen
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = MarketLightGreen
                                            ) {
                                                Text(
                                                    text = "Match: ${rec.matchScore}%",
                                                    modifier = Modifier.padding(
                                                        horizontal = 10.dp,
                                                        vertical = 6.dp
                                                    ),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MarketGreen
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Net Return",
                                                    fontSize = 12.sp,
                                                    color = MarketMuted
                                                )
                                                Text(
                                                    text = "KES ${"%.2f".format(rec.netReturnPerKg)} per kg",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MarketBrown
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Estimated Total Return",
                                                    fontSize = 12.sp,
                                                    color = MarketMuted
                                                )
                                                Text(
                                                    text = "KES ${"%.2f".format(rec.totalEstimatedReturn)}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MarketGreen
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        HorizontalDivider(
                                            color = Color(0xFFECE5DB)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "💡Analysis and Recommendation: ",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MarketBrown
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = rec.recommendationReason,
                                            fontSize = 12.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📍${rec.location.ifBlank { "Location N/A" }}",
                                                fontSize = 12.sp,
                                                color = MarketMuted
                                            )
                                            val existingRequest = myLinkageRequests.find { req ->
                                                val reqBuyer = (req["buyer_name"] as? String) ?: ""
                                                reqBuyer.equals(rec.buyerName, ignoreCase = true)
                                            }

                                            if (existingRequest != null) {
                                                val reqStatus = (existingRequest["status"] as? String) ?: "Pending"
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = when (reqStatus.lowercase()) {
                                                        "completed" -> Color(0xFFC8E6C9)
                                                        "processing" -> Color(0xFFBBDEFB)
                                                        else -> Color(0xFFFFE0B2)
                                                    }
                                                ) {
                                                    Text(
                                                        text = "Status: $reqStatus",
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (reqStatus.lowercase()) {
                                                            "completed" -> MarketGreen
                                                            "processing" -> Color(0xFF1976D2)
                                                            else -> MarketBrown
                                                        }
                                                    )
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        val currentUserId = auth.currentUser?.uid
                                                        if (currentUserId == null) {
                                                            Toast.makeText(context, "Please log in first", Toast.LENGTH_SHORT).show()
                                                            return@Button
                                                        }
                                                        val linkageRequest = hashMapOf(
                                                            "farmer_id" to currentUserId,
                                                            "farmer_name" to (auth.currentUser?.displayName ?: "Farmer"),
                                                            "cooperative_id" to (cooperativeId ?: ""),
                                                            "buyer_name" to rec.buyerName,
                                                            "coffee_type" to rec.coffeeType,
                                                            "grade" to rec.grade,
                                                            "offered_price_per_kg" to rec.offeredPricePerKg,
                                                            "net_return_per_kg" to rec.netReturnPerKg,
                                                            "location" to rec.location,
                                                            "status" to "Pending",
                                                            "date_requested" to System.currentTimeMillis()
                                                        )
                                                        FirebaseFirestore.getInstance()
                                                            .collection("market_links")
                                                            .add(linkageRequest)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(context, "Link request sent", Toast.LENGTH_LONG).show()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Toast.makeText(context, "Failed to send link request: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                                            }
                                                    },
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MarketGreen),
                                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Text(text = "Request Linkage", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                    text = "Market Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MarketBrown
                )
            Spacer(modifier = Modifier.height(5.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search coffee type or cooperative") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MarketGreen,
                    unfocusedBorderColor = MarketBrown.copy(alpha = 0.5f),
                    focusedLabelColor = MarketGreen,
                    cursorColor = MarketGreen,
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box {
                OutlinedButton(
                    onClick = { showFilterMenu = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MarketGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("Filter")
                    Spacer(modifier = Modifier.width(5.dp))
                }
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Filter by Coffee types") },
                        onClick = {
                            showFilterMenu = false
                            showCoffeeMenu = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Filter by Cooperatives") },
                        onClick = {
                            showFilterMenu = false
                            showCooperativeMenu = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear Filters") },
                        onClick = {
                            selectedCoffeeType = "All types"
                            selectedCooperative = "All cooperatives"
                            searchText = ""
                            showFilterMenu = false
                        }
                    )
                }
                DropdownMenu(
                    expanded = showCoffeeMenu,
                    onDismissRequest = { showCoffeeMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All types") },
                        onClick = {
                            selectedCoffeeType = "All types"
                            showCoffeeMenu = false
                        }
                    )
                    coffeeTypes.forEach { coffeeType ->
                        DropdownMenuItem(
                            text = { Text(coffeeType) },
                            onClick = {
                                selectedCoffeeType = coffeeType
                                showCoffeeMenu = false
                            }
                        )
                    }
                }
                DropdownMenu(
                    expanded = showCooperativeMenu,
                    onDismissRequest = { showCooperativeMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All cooperatives") },
                        onClick = {
                            selectedCooperative = "All cooperatives"
                            showCooperativeMenu = false
                        }
                    )
                    cooperatives.forEach { cooperative ->
                        DropdownMenuItem(
                            text = { Text(cooperative.second) },
                            onClick = {
                                selectedCooperative = cooperative.first
                                showCooperativeMenu = false
                            }
                        )
                    }
                }
            }

            if (selectedCoffeeType != "All types" || selectedCooperative != "All cooperatives") {
                Spacer(modifier = Modifier.height(10.dp))
                val coopName = cooperatives.firstOrNull { it.first == selectedCooperative }?.second ?: "All cooperatives"
                Text(
                    text = "Filtered by: $selectedCoffeeType | $coopName",
                    fontSize = 14.sp,
                    color = MarketGreen
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Coffee Prices",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MarketBrown
                )
                Text(
                    text = "${filteredPrices.size} results",
                    fontSize = 12.sp,
                    color = MarketMuted
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            when {
                isLoading ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MarketGreen)
                    }

                loadError -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading prices",
                            color = MarketBrown
                        )
                    }
                }

                filteredPrices.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cooperativeId.isNullOrBlank()) {
                                "Please join a cooperative to view your cooperative's prices"
                            } else if (prices.isEmpty()) {
                                "No prices published by your cooperative yet"
                            } else {
                                "No prices match your search or filters"
                            },
                            color = MarketMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = filteredPrices,
                            key = { it.id }
                        ) { marketPrice ->
                            val priceChange = marketPrice.price - marketPrice.previousPrice

                            Card(
                                modifier = Modifier.fillMaxWidth(),
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = marketPrice.coffeeType,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MarketBrown
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = marketPrice.cooperativeName.ifBlank {
                                                    if (marketPrice.cooperativeId.isNotBlank()) {
                                                        "Cooperative ${marketPrice.cooperativeId.takeLast(5)}"
                                                    } else {
                                                        "Cooperative (UNKNOWN)"
                                                    }
                                                },
                                                fontSize = 12.sp,
                                                color = MarketMuted
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MarketCream
                                        ) {
                                            Text(
                                                text = "☕",
                                                modifier = Modifier.padding(8.dp),
                                                fontSize = 20.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Current Price",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MarketMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "${marketPrice.currency} ${"%,d".format(marketPrice.price)} ",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MarketGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = marketPrice.priceUnit,
                                            fontSize = 12.sp,
                                            color = MarketMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(
                                        color = Color(0xFFECE5DB)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Previous Price",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MarketMuted
                                            )
                                            Text(
                                                text = "${marketPrice.currency} ${"%,d".format(marketPrice.previousPrice)}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MarketBrown
                                            )
                                        }
                                        val movementText: String
                                        val movementColor: Color
                                        val movementBackground: Color

                                        when {
                                            marketPrice.previousPrice == 0L -> {
                                                movementText = "New Price"
                                                movementColor = MarketBrown
                                                movementBackground = MarketCream
                                            }

                                            priceChange > 0L -> {
                                                movementText = "Increased ${marketPrice.currency} ${"%,d".format(priceChange)}"
                                                movementColor = MarketGreen
                                                movementBackground = MarketLightGreen
                                            }

                                            priceChange < 0L -> {
                                                movementText = "Decreased ${marketPrice.currency} ${"%,d".format(-priceChange)}"
                                                movementColor = MarketRed
                                                movementBackground = MarketLightRed
                                            }

                                            else -> {
                                                movementText = "No change"
                                                movementColor = MarketMuted
                                                movementBackground = MarketCream
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = movementBackground
                                        ) {
                                            Text(
                                                text = movementText,
                                                modifier = Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 7.dp
                                                ),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = movementColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
