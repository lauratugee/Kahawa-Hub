package com.example.kahawahub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.example.kahawahub.ui.theme.KahawaHubTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.foundation.BorderStroke


private val CoffeeCream = Color(0xFFF7F3E9)
private val CoffeeBrown = Color(0xFF684832)
private val ForestGreen = Color(0xFF245640)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KahawaHubTheme {
                KahawaHubApp()

                }
            }
        }
}


@Composable
fun KahawaHubApp() {
    val context= LocalContext.current

    val notificationPermissionLauncher=
        rememberLauncherForActivityResult(
            contract= ActivityResultContracts.RequestPermission()
        ) {  isGranted ->
            Log.d(
                "KahawaNotifications",
                "Notification permission granted: $isGranted"
            )
        }
    LaunchedEffect(Unit){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            val permissionGranted=
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if(!permissionGranted){
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    var currentScreen by remember {
        mutableStateOf("welcome")
    }
    var profileReturnScreen by remember {
        mutableStateOf("farmerDashbpard")

    }
    when (currentScreen) {

        "welcome" -> {
            KahawaHubWelcomePage(
                onLoginClick = {
                    currentScreen = "login"
                },
                onRegistrationClick = {
                    currentScreen = "register"
                }
            )
        }

        "login" -> {
            LoginPage(
                onRegistrationClick = {
                    currentScreen = "register"
                },
                onLoginSuccess = { userType ->
                    saveFcmForCurrentUser()
                    currentScreen= when(userType){
                        "Farmer" -> "farmerDashboard"
                        "Cooperative Society" -> "cooperativeDashboard"
                        else -> "welcome"

                    }

                }
            )
        }

        "register" -> {
            RegistrationPage(
                onLoginClick = {
                    currentScreen = "login"
                }
            )
        }
        "farmerDashboard" ->{
            FarmerDashboard(
                onFindCooperativeClick = {
                    currentScreen="findCooperative"
                },
                onViewCoffeePricesClick = {
                    currentScreen="coffeeMarketPrices"
                },
                onProfileClick = {
                    profileReturnScreen="farmerDashboard"
                    currentScreen="myProfile"
                },
                onAnnouncementsClick = {
                    currentScreen="announcements"
                }
            )
        }
        "findCooperative" -> {
            FindCooperativePage(
                onBackClick = { currentScreen="farmerDashboard"}
            )
        }
        "coffeeMarketPrices" -> {
            CoffeeMarketPricesPage(
                onBackClick = { currentScreen="farmerDashboard"}
            )

        }
        "cooperativeDashboard" -> {
            CooperativeDashboard(
                onManagePricesClick = {
                    currentScreen = "coffeePrices"
                },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    currentScreen="welcome"
                }

            )
        }
        "coffeePrices" -> {
            CoffeePrices(
                onBackClick={
                    currentScreen="cooperativeDashboard"
                }
            )
        }
        "myProfile" -> {
            ProfilePage (
                onBackClick = {
                    currentScreen=profileReturnScreen
                },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    currentScreen="welcome"
                }
            )
        }
        "announcements" -> {
            AnnouncementsPage(
                onBackClick = {
                    currentScreen="farmerDashboard"
                }
            )
        }
    }


}
private fun saveFcmForCurrentUser(){
    val userId= FirebaseAuth.getInstance().currentUser?.uid
        ?: return

    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(
                    mapOf("fcm_token" to token),
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    Log.d(
                        "KahawaFCM",
                        "FCM token saved successfully"
                    )
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "KahawaFCM",
                        "Could not save FCM token",
                        exception
                    )
                }
        }
        .addOnFailureListener { exception ->
            Log.e(
                "KahawaFCM",
                "Could not retrieve FCM token",
                exception
            )
        }
}

@Composable
fun KahawaHubWelcomePage(
    onLoginClick: () -> Unit,
    onRegistrationClick: () -> Unit
){
    Box(
        modifier=Modifier.fillMaxSize()
    ){
        Image(
            painter=painterResource(
                id=R.drawable.welcomepagepic
            ),
            contentDescription = "Coffee farm background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier=Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.45f))
        )
    }
    Column(
        modifier= Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 26.dp, vertical=24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.Center
    ){

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text= "KAHAWA HUB",
            fontSize = 38.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,
            color=CoffeeCream,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text= "Your Coffee. Your Market.",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
            color=CoffeeCream,
            textAlign = TextAlign.Center

            )
            Spacer(modifier=Modifier.height(32.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape= RoundedCornerShape(14.dp),
                colors= ButtonDefaults.buttonColors(
                    containerColor=ForestGreen
                )
            ){
            Text(
                text = "Login",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold

            )
        }
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onRegistrationClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape= RoundedCornerShape(14.dp),
                colors=ButtonDefaults.outlinedButtonColors(
                    containerColor = CoffeeCream.copy(alpha=0.9f),
                    contentColor=ForestGreen
                ),
                border=androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    ForestGreen
                )
            ) {
                Text(
                    text="Register",
                    fontSize = 16.sp
                )
            }


        }
        }







