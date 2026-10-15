package com.example.kahawahub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class AnnouncementItem(
    val id: String,
    val title: String,
    val message: String,
    val createdAt: Long
)

@Composable
fun AnnouncementsPage (
    onBackClick: () -> Unit
) {
    val db= FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val announcements by remember {
        mutableStateOf<List<AnnouncementItem>>(emptyList())

    }
    var isLoading by remember { mutableStateOf(true) }
    var hasCooperative by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(userId){
        if(userId==null){
            isLoading=false
            errorMessage="Please log in to view announcements"
        }
}
}