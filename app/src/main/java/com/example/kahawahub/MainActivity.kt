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

import com.example.kahawahub.ui.theme.KahawaHubTheme

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

    var currentScreen by remember {
        mutableStateOf("welcome")
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
                    currentScreen= when(userType){
                        "Farmer" -> "farmerDahsboard"
                        "Cooperative Society" -> "cooperativeDashboard"
                        else -> "Welcome"

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
            FarmerDashboard()
        }
        "cooperativeDashboard" -> {
            CooperativeDashboard()
        }
    }


}

@Composable
fun KahawaHubWelcomePage(
    onLoginClick: () -> Unit,
    onRegistrationClick: () -> Unit
){
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter=painterResource(
                id=R.drawable.welcomepagepic
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop

        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background((
                        Color.Black.copy(alpha=0.35f)
                        )

                )
        )
    }
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(
                    start = 30.dp,
                    end = 30.dp,
                    top =20.dp,
                    bottom =20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
          Text(
              text= "KAHAWA HUB",
              fontSize = 32.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
          )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text= "Your Coffee. Your Market.",
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier=Modifier.height(50.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ){
            Text(
                text = "Login",
                fontSize = 16.sp

            )
        }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRegistrationClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text="Register",
                    fontSize = 16.sp
                )
            }


        }
        }

    }





