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
import com.google.firebase.firestore.FirebaseFirestore


private val MarketCream = Color(0xFFF7F3E9)
private val MarketBrown = Color(0xFF684832)
private val MarketGreen = Color(0xFF245640)
private val MarketMuted = Color(0xFF81766C)
private val MarketRed= Color(0xFFB4483E)
private val MarketLightGreen = Color(0xFFE4EFE5)
private val MarketLightRed = Color(0xFFF8E8E5)

data class MarketPrice(
    val coffeeType : String = "",
    val price: Long = 0L,
    val previousPrice : Long = 0L,
    val currency : String = "",
    val priceUnit: String = "",
    val cooperativeName: String = ""
)

@Composable
fun CoffeeMarketPricesPage(
    onBackClick: () -> Unit
) {
    var prices by remember {
        mutableStateOf<List<MarketPrice>>(emptyList())
    }
    var isLoading by remember {
        mutableStateOf(true)
    }
   DisposableEffect(Unit) {
       val listener = FirebaseFirestore.getInstance()
           .collection("coffee_prices")
           .addSnapshotListener { documents, error ->

               if (error !=null){
                   isLoading=false
                   return@addSnapshotListener
               }

               if (documents != null) {
                   prices=documents.documents.map { document ->
                       MarketPrice(
                           coffeeType = document.getString("coffee_type") ?: "",
                           price = document.getLong("price") ?: 0L,
                           previousPrice = document.getLong("previous_price") ?: 0L,
                           currency = document.getString("currency") ?: "KES",
                           priceUnit = document.getString("price_unit") ?: "per kg",
                           cooperativeName = document.getString("cooperative_name") ?: ""
                       )
                   }
                   isLoading=false
                   }
               }
       onDispose {
           listener.remove()
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
                modifier= Modifier
                    .fillMaxWidth()
                    .weight(1f),
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