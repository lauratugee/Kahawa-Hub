package com.example.kahawahub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

data class MarketPrice(
    val coffeeType : String = "",
    val price: Long = 0L,
    val previousPrice : Long = 0L,
    val currency : String = "",
    val priceUnit: String = "",
    val cooperativeName: String = ""
)

@Composable
fun CoffeeMarketPricesPage() {
    var prices by remember {
        mutableStateOf<List<MarketPrice>>(emptyList())
    }
    var isLoading by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(Unit) {

        FirebaseFirestore.getInstance()
            .collection("coffee_prices")
            .get()
            .addOnSuccessListener { documents ->

                prices=documents.documents.map { document ->

                    MarketPrice(
                        coffeeType = document.getString("coffee_type") ?: "",
                        price=document.getLong("price") ?: 0L,
                        previousPrice = document.getLong("previous_price") ?: 0L,
                        currency=document.getString("currency") ?: "KES",
                        priceUnit=document.getString("price_unit") ?: "per kg",
                        cooperativeName =
                            document.getString("cooperative_name") ?: ""
                    )
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ){
        Text(
            text="Coffee Market Prices",
            style= MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier= Modifier.height(8.dp))

        Text(
            text="View the latest coffee prices from cooperatives",
            style=MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(20.dp))

        if(isLoading) {
            CircularProgressIndicator()

        } else if (prices.isEmpty()){
            Text(
                text="No coffee prices available yet"
            )
        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(prices){ marketPrice ->
                    val priceChange=
                        marketPrice.price - marketPrice.previousPrice

                    Card(
                        modifier=Modifier.padding(16.dp)
                    ){
                        Text(
                            text=marketPrice.coffeeType,
                            style= MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier=Modifier.height(8.dp))

                        Text(
                            text="Current Price: ${marketPrice.currency} ${marketPrice.price} ${marketPrice.priceUnit}"
                        )
                        Text(
                            text="Previous Price: ${marketPrice.currency} ${marketPrice.previousPrice} ${marketPrice.priceUnit}"
                        )
                        Spacer(modifier=Modifier.height(6.dp))

                        Text(
                            text= if (priceChange > 0) {
                                "Price increased by ${marketPrice.currency} $priceChange ${marketPrice.priceUnit}"
                            } else if (priceChange < 0) {
                                "Price decreased by ${marketPrice.currency} ${-priceChange} ${marketPrice.priceUnit}"
                            } else {
                                "Price has not changed"
                            }
                        )

                        if (marketPrice.cooperativeName.isNotEmpty()){

                            Spacer(modifier=Modifier.height(6.dp))

                            Text(
                                text="Provided by: ${marketPrice.cooperativeName}"
                            )
                        }
                    }
                }
            }
        }

    }

}