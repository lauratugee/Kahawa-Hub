package com.example.kahawahub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private val SupplyBrown = Color(0xFF684832)
private val SupplyGreen = Color(0xFF245640)
private val SupplyCream = Color(0xFFF7F3E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoffeeSupplyPage() {
    var selectedCoffeeType by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var coffeeTypeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SupplyCream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "My Coffee Supply",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SupplyBrown
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Manage your coffee supply",
            color = Color.DarkGray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Coffee Type",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SupplyBrown
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox (
            expanded = coffeeTypeExpanded,
            onExpandedChange = {
                coffeeTypeExpanded = !coffeeTypeExpanded
            }
        ) {
            OutlinedTextField(
                value = selectedCoffeeType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select coffee type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = coffeeTypeExpanded
                    )
                },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = coffeeTypeExpanded,
                onDismissRequest = {
                    coffeeTypeExpanded = false
                }
            ) {
                listOf(
                    "Coffee Cherries",
                    "Mbuni",
                    "Pulped Coffee"
                ).forEach { coffeeType ->

                    DropdownMenuItem(
                        text = { Text(coffeeType) },
                        onClick = {
                            selectedCoffeeType = coffeeType
                            coffeeTypeExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Quantity available",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = SupplyBrown
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    quantity = newValue
                }
            },
            label = { Text("Enter quantity in kgs") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
            },

            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SupplyGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Submit coffee supply")
        }
    }
}







