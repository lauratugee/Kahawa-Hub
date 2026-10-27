package com.example.kahawahub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.text.font.Font
import com.google.firebase.firestore.FirebaseFirestore
import java.nio.file.WatchEvent


private val MarketCream = Color(0xFFF7F3E9)
private val MarketBrown = Color(0xFF684832)
private val MarketGreen = Color(0xFF245640)
private val MarketMuted = Color(0xFF81766C)
private val MarketRed= Color(0xFFB4483E)
private val MarketLightGreen = Color(0xFFC8E6C9)
private val MarketLightRed = Color(0xFFFFCDD2)

data class MarketPrice(
    val id: String = "",
    val coffeeType : String = "",
    val price: Long = 0L,
    val previousPrice : Long = 0L,
    val currency : String = "KES",
    val priceUnit: String = "per kg",
    val cooperativeName: String = "",
    val cooperativeId: String = ""
)

@Composable
fun CoffeeMarketPricesPage(onBackClick: () -> Unit) {

    var prices by remember {
        mutableStateOf<List<MarketPrice>>(emptyList())
    }
    var isLoading by remember {
        mutableStateOf(true)
    }
    var loadError by remember {
        mutableStateOf(false)
    }
    var searchText by remember { mutableStateOf("")}
    var selectedCoffeeType by remember { mutableStateOf("All types") }
    var selectedCooperative by remember { mutableStateOf("All cooperatives") }

    var showFilterMenu by remember { mutableStateOf(false) }
    var showCoffeeMenu by remember { mutableStateOf(false) }
    var showCooperativeMenu by remember { mutableStateOf(false) }


   DisposableEffect(Unit) {
       val listener = FirebaseFirestore.getInstance()
           .collection("coffee_prices")
           .addSnapshotListener { documents, error ->

               if (error !=null){
                   loadError=true
                   isLoading=false
                   return@addSnapshotListener
               }

               if (documents != null) {
                   prices=documents.documents.map { document ->
                       val cooperativeId = document.getString("cooperative_id") ?: ""

                       MarketPrice(
                           id=document.id,
                           coffeeType = document.getString("coffee_type") ?: "",
                           price = document.getLong("price") ?: 0L,
                           previousPrice = document.getLong("previous_price") ?: 0L,
                           currency = document.getString("currency") ?: "KES",
                           priceUnit = document.getString("price_unit") ?: "per kg",
                           cooperativeName = document.getString("cooperative_name") ?: "",
                           cooperativeId = cooperativeId
                       )
                   }
                   loadError=false
                   isLoading=false
                   }
               }
       onDispose {
           listener.remove()
               }
   }
    val coffeeTypes = prices
        .map{ it.coffeeType }
        .filter{ it.isNotBlank() }
        .distinctBy { it.lowercase() }
        .sorted()

        val cooperatives = prices
            .map { marketPrice ->
                val label=marketPrice.cooperativeName.ifBlank {
                    if (marketPrice.cooperativeId.isNotBlank()) {
                        "Cooperative  ${marketPrice.cooperativeId.takeLast(5)}"
                    } else {
                        "Cooperative (UNKNOWN)"
                    }
                }
                marketPrice.cooperativeId to label
            }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase()}

    val filteredPrices=prices.filter { marketPrice ->

        val cooperativeLabel = marketPrice.cooperativeName.ifBlank {
            if (marketPrice.cooperativeId.isNotBlank()) {
                "Cooperative  ${marketPrice.cooperativeId.takeLast(5)}"
            } else {
                "Cooperative (UNKNOWN)"
            }
        }
        val matchesSearch=
            marketPrice.coffeeType.contains(searchText, ignoreCase=true) ||
                    cooperativeLabel.contains(searchText, ignoreCase=true)

        val matchesCoffee =
            selectedCoffeeType == "All types" ||
                    marketPrice.coffeeType.equals(
                        selectedCoffeeType,
                        ignoreCase = true
                    )

        val matchesCooperative =
            selectedCooperative == "All cooperatives" ||
                    marketPrice.cooperativeId == selectedCooperative

        matchesSearch && matchesCoffee && matchesCooperative

        }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ){

        Text(
            text="Coffee Market Prices",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MarketBrown
        )
        Spacer(modifier= Modifier.height(5.dp))

        Text(
            text="Your Coffee. Your Market.",
            fontSize = 14.sp,
            color= MarketMuted
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier=Modifier.fillMaxWidth(),
            singleLine=true,
            placeholder = {
                Text("Search")
            },
            leadingIcon = {
                Text("🔎", fontSize = 20.sp,color = MarketGreen)
            },
            shape= RoundedCornerShape(14.dp),
            colors= OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MarketGreen,
                unfocusedBorderColor = MarketBrown.copy(alpha = 0.5f),
                focusedLabelColor = MarketGreen,
                cursorColor = MarketGreen,
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        Box{
            OutlinedButton(
                onClick = { showFilterMenu = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MarketGreen
            )
            ){
                Text("Filter")
                Spacer(modifier = Modifier.width(5.dp))
            }
            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false }
            ){
                DropdownMenuItem(
                    text = { Text("Filter by Coffee types") },
                    onClick = {
                        showFilterMenu=false
                        showCoffeeMenu = true
                    }
                )
                DropdownMenuItem(
                    text = { Text(" Filter by Cooperatives") },
                    onClick = {
                        showFilterMenu=false
                        showCooperativeMenu = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Clear Filters") },
                    onClick = {
                        selectedCoffeeType = "All types"
                        selectedCooperative = "All cooperatives"
                        searchText = ""
                        showFilterMenu=false
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
            ){
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
        if (selectedCoffeeType != "All types" ||
            selectedCooperative != "All cooperatives"
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Filtered Results" +
                        (if (selectedCoffeeType != "All types") {
                            selectedCoffeeType
                        } else {
                            "All coffee types"
                        }) +
                        "" +
                        (cooperatives.firstOrNull {
                            it.first == selectedCooperative
                        }?.second ?: "All cooperatives"),
                fontSize = 20.sp,
                color = MarketGreen
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier=Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text="Coffee Prices",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MarketBrown
            )
            Text(
                text= "${filteredPrices.size} results",
                fontSize = 12.sp,
                color=MarketMuted
            )

        }
        Spacer(modifier = Modifier.height(10.dp))

        when {
            isLoading ->
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MarketGreen)
                }

            loadError -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
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
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (prices.isEmpty()) {
                            "No prices available yet"
                        } else {
                            "No prices match your search or filters"
                        },
                        color = MarketMuted
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

                        val priceChange =
                            marketPrice.price - marketPrice.previousPrice

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
                                        Spacer(
                                            modifier = Modifier.height(4.dp)
                                        )
                                        Text(
                                            text = marketPrice.cooperativeName
                                                .ifBlank {
                                                    if (marketPrice.cooperativeId.isNotBlank()) {
                                                        "Cooperative ${
                                                            marketPrice.cooperativeId.takeLast(
                                                                5
                                                            )
                                                        }"
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
                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                                Text(
                                    text = "Current Price",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MarketMuted
                                )
                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "${marketPrice.currency} ${"%,d".format(marketPrice.price)} ",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MarketGreen
                                    )
                                    Spacer(
                                        modifier = Modifier.width(4.dp)
                                    )
                                    Text(
                                        text = marketPrice.priceUnit,
                                        fontSize = 12.sp,
                                        color = MarketMuted
                                    )
                                }
                                Spacer(
                                    modifier = Modifier.height(14.dp)
                                )
                                HorizontalDivider(
                                    color = Color(0xFFECE5DB)
                                )
                                Spacer(
                                    modifier = Modifier.height(14.dp)
                                )
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
                                            text = "${marketPrice.currency} ${
                                                "%,d".format(
                                                    marketPrice.previousPrice
                                                )
                                            }",
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
                                            movementText = "Increased ${marketPrice.currency} ${
                                                "%,d".format(priceChange)
                                            }"
                                            movementColor = MarketGreen
                                            movementBackground = MarketLightGreen
                                        }

                                        priceChange < 0L -> {
                                            movementText =
                                                "Decreased ${marketPrice.currency} ${"%,d".format(-priceChange)}"
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




















