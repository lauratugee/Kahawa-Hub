package com.example.kahawahub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.ui.text.font.FontWeight

@Composable
fun  CooperativeDashboard(){
    val userName = remember { mutableStateOf("Cooperative Society") }
    val announcementTitle = remember { mutableStateOf("")}
    val announcementMessage= remember { mutableStateOf("")}

    LaunchedEffect(Unit){
        val userId = FirebaseAuth.getInstance().currentUser?.uid



        if(userId != null){
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener{ document ->
                    userName.value=
                        document.getString("name") ?: "Cooperative Society"

                }
        }
    }
    Column(
        modifier=Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text="COOPERATIVE DASHBOARD",
            fontSize=24.sp
        )
        Text(
            text="Welcome, ${userName.value}",
            fontSize=18.sp
        )
        Spacer(modifier=Modifier.height(24.dp))

        Text(
            text="Create Announcement",
            fontSize= 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier= Modifier.height(12.dp))

        OutlinedTextField(
            value=announcementTitle.value,
            onValueChange = {
                announcementTitle.value = it
            },
            label={
                Text("Announcement Title")
            },
            modifier=Modifier.fillMaxWidth(),

        )
        Spacer(modifier=Modifier.height(12.dp))

        OutlinedTextField(
            value=announcementMessage.value,
            onValueChange = {
                announcementMessage.value=it
            },
            label={
                Text("Announcement Message")
            },
            modifier=Modifier.fillMaxWidth(),
            minLines=4
        )
        Spacer(modifier=Modifier.height(16.dp))

        Button(
            onClick={
                val userId= FirebaseAuth.getInstance().currentUser?.uid

                if(userId !=null) {
                    val announcement = hashMapOf(
                        "title" to announcementTitle.value,
                        "message" to announcementMessage.value,
                        "posted_by" to userId,
                        "date_posted" to System.currentTimeMillis(),
                        "target_audience" to "Farmers"
                    )
                    FirebaseFirestore.getInstance()
                        .collection("announcements")
                        .add(announcement)
                }

            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Post Announcement")


        }

    }
}