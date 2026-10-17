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
import java.nio.file.WatchEvent
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

    var announcements by remember {
        mutableStateOf<List<AnnouncementItem>>(emptyList())

    }
    var isLoading by remember { mutableStateOf(true) }
    var hasCooperative by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(userId){
        if(userId==null){
            isLoading=false
            errorMessage="Please log in to view announcements"
        } else {
            db.collection("farmers")
                .document(userId)
                .get()
                .addOnSuccessListener { farmerDocument ->

                    val cooperativeId=
                        farmerDocument.getString("cooperative_id")

                    if(cooperativeId.isNullOrBlank()){
                        hasCooperative=false
                        isLoading=false

                    } else {
                        hasCooperative=true

                        db.collection("announcements")
                            .whereEqualTo(
                                "cooperative_id",
                                cooperativeId
                            )
                            .get()
                            .addOnSuccessListener { result ->

                                announcements=result.documents.map { document ->
                                    AnnouncementItem(
                                        id=document.id,
                                        title=document.getString("title") ?: "Announcement",
                                        message=document.getString("message")
                                            ?: document.getString("body")
                                            ?: "",
                                        createdAt=
                                            document.getLong("date_posted")
                                                ?: document.getLong("created_at")
                                                ?: 0L
                                    )
                                }.sortedByDescending { it.createdAt }

                                isLoading=false
                            }
                            .addOnFailureListener {
                                errorMessage=
                                    "Could not load announcements.Please try again"
                                isLoading=false
                            }
                    }
                }
                .addOnFailureListener {
                    errorMessage=
                        "Could not load your cooperative details"
                    isLoading=false
                }


        }
}
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)

    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){
            TextButton(onClick=onBackClick){
                Text("Back")
            }
            Text(
                text="Announcements",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text="Updates",
            style=MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(20.dp))

        when{
            isLoading -> {
                CircularProgressIndicator(
                    modifier=Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )
            }
            errorMessage.isNotBlank() -> {
                Text(errorMessage)
            }
            !hasCooperative -> {
                Text(
                    "No announcements available. Join a cooperative to receive announcements"
                )
            }
            announcements.isEmpty() -> {
                Text("No announcements yet")
            }
             else -> {
                 LazyColumn(
                     verticalArrangement = Arrangement.spacedBy(12.dp)

                 ) {
                     items(
                         items=announcements,
                         key={it.id}
                     ){ announcement ->

                         Card(
                             modifier = Modifier.fillMaxWidth()
                         ){
                             Column(
                                 modifier=Modifier.padding(16.dp),
                                 verticalArrangement = Arrangement.spacedBy(8.dp)
                             ){
                                 Text(announcement.message)

                                 if(announcement.createdAt > 0L){
                                     Text(
                                         text= SimpleDateFormat(
                                             "dd MMM yyyy, HH:mm",
                                             Locale.getDefault()
                                         ).format(
                                             Date(announcement.createdAt)
                                         ),
                                         style=MaterialTheme.typography.bodySmall,
                                         color= MaterialTheme.colorScheme
                                             .onSurfaceVariant
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