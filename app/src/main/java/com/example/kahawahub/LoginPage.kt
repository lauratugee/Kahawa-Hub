package com.example.kahawahub

import android.R
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

private val LoginCream = Color(0xFFF7F3E9)
private val LoginBrown = Color(0xFF684832)
private val LoginGreen = Color(0xFF245640)

@Composable
fun LoginPage(
    onRegistrationClick: () -> Unit,
    onLoginSuccess:(String) -> Unit
    ){

    var email by remember{
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var rememberMe by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginCream)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "KAHAWA HUB",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = LoginBrown
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Login to your account",
            fontSize = 15.sp,
            color = LoginBrown.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            },
            label = {
                Text("Email")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoginGreen,
                unfocusedBorderColor = LoginBrown.copy(alpha = 0.5f),
                focusedLabelColor = LoginGreen,
                cursorColor = LoginGreen,
                focusedTextColor = LoginBrown,
                unfocusedTextColor = LoginBrown
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("Password")
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LoginGreen,
                unfocusedBorderColor = LoginBrown.copy(alpha = 0.5f),
                focusedLabelColor = LoginGreen,
                cursorColor = LoginGreen,
                focusedTextColor = LoginBrown,
                unfocusedTextColor = LoginBrown
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = {
                    rememberMe = it
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = LoginGreen
                )
            )
            Text(
                text = "Remember me",
                color = LoginBrown,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Forgot password?",
                color = LoginGreen,
                fontSize = 12.sp
            )
        }
    }

        Button(
            onClick = {
                if(email.isBlank() || password.isBlank()){
                    Toast.makeText(
                        context,
                        "Please enter your email and password",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid

                            if (userId != null) {
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener { document ->

                                        val userType = document.getString("user_type") ?: ""

                                        Toast.makeText(
                                            context,
                                            "Login successful",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        onLoginSuccess(userType)
                                    }
                            }
                        }

                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                context,
                                "Login failed:(${exception.javaClass.simpleName}): ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }

            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape= RoundedCornerShape(14.dp),
            colors= ButtonDefaults.buttonColors(
                containerColor = LoginGreen
            )
        ) {
            Text(
                text="Login",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                color=LoginBrown,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(5.dp))

            TextButton(
                onClick = onRegistrationClick
            ) {

                Text(
                    text = "Sign up",
                    color=LoginGreen,
                    fontWeight = FontWeight.Bold
                )
            }


        }
    }

