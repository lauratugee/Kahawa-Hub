package com.example.kahawahub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MapBrown = Color(0xFF684832)
private val MapGreen = Color(0xFF245640)
private val MapRed = Color(0xFFD32F2F)

@Composable
fun CooperativeMapCard(
    cooperativeName: String,
    locationString: String
) {
    // Resolve precise regional coordinates for the location string
    val (lat, lng, regionDisplayName) = remember(locationString) {
        getCoordinatesForRegion(locationString)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFEBE9)),
            contentAlignment = Alignment.Center
        ) {
            // Abstract Canvas Vector Map representation with grid & trade routes
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw background grid lines representing regional map grid
                val gridColor = Color.LightGray.copy(alpha = 0.5f)
                for (i in 1..5) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, i * (canvasHeight / 6f)),
                        end = Offset(canvasWidth, i * (canvasHeight / 6f)),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(i * (canvasWidth / 6f), 0f),
                        end = Offset(i * (canvasWidth / 6f), canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw connecting trade routes / transport paths
                drawLine(
                    color = MapGreen.copy(alpha = 0.4f),
                    start = Offset(canvasWidth * 0.3f, canvasHeight * 0.7f),
                    end = Offset(canvasWidth * 0.5f, canvasHeight * 0.4f),
                    strokeWidth = 3.dp.toPx()
                )
                drawLine(
                    color = MapBrown.copy(alpha = 0.4f),
                    start = Offset(canvasWidth * 0.5f, canvasHeight * 0.4f),
                    end = Offset(canvasWidth * 0.75f, canvasHeight * 0.65f),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Overlay Information Card & Pin
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MapGreen
                    ) {
                        Text(
                            text = "📍 Region: $regionDisplayName",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "GPS: %.4f, %.4f".format(lat, lng),
                            color = MapBrown,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Center Pin Badge
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MapRed, CircleShape)
                        ) {
                            // Pulse dot effect representation
                        }
                        Column {
                            Text(
                                text = cooperativeName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MapBrown
                            )
                            Text(
                                text = "Verified Coffee Hub & Collection Center",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Bottom Distance Info
                Text(
                    text = "Connected to Regional Mill & Transport Network",
                    fontSize = 11.sp,
                    color = MapBrown,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// Helper for exact regional coordinates in Kenya
private fun getCoordinatesForRegion(location: String): Triple<Double, Double, String> {
    val cleanLoc = location.trim().lowercase()

    val regions = mapOf(
        "nyeri" to Triple(-0.4169, 36.9515, "Nyeri County"),
        "kericho" to Triple(-0.3700, 35.2831, "Kericho County"),
        "kiambu" to Triple(-1.1714, 36.8356, "Kiambu County"),
        "embu" to Triple(-0.5311, 37.4590, "Embu County"),
        "kirinyaga" to Triple(-0.5000, 37.3333, "Kirinyaga County"),
        "murang'a" to Triple(-0.7172, 37.1528, "Murang'a County"),
        "muranga" to Triple(-0.7172, 37.1528, "Murang'a County"),
        "bomet" to Triple(-0.7813, 35.3444, "Bomet County"),
        "kisii" to Triple(-0.6817, 34.7667, "Kisii County"),
        "meru" to Triple(0.0463, 37.6499, "Meru County"),
        "nairobi" to Triple(-1.2921, 36.8219, "Nairobi Hub"),
        "nakuru" to Triple(-0.3031, 36.0800, "Nakuru County")
    )

    for ((region, coords) in regions) {
        if (cleanLoc.contains(region)) {
            return coords
        }
    }

    return Triple(-0.4169, 36.9515, location.ifBlank { "Central Kenya" })
}
