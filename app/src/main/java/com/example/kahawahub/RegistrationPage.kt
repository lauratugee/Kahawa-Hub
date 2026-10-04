package com.example.kahawahub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun RegistrationPage(
    onLoginClick: () ->Unit
    ){
    var fullName by remember {
        mutableStateOf("")
    }
    var email by remember {
        mutableStateOf("")
    }
    var phoneNumber by remember {
        mutableStateOf("")
    }
    var password by remember{
        mutableStateOf("")
    }
    var confirmPassword by remember {
        mutableStateOf("")
    }
    var userType by remember{
        mutableStateOf("")
    }
    var userTypeExpanded by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement= Arrangement.Center
    ){
        Text(
            text="KAHAWA HUB",
            fontSize=28.sp,
            fontWeight=FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text="Create your account",
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(25.dp))

        OutlinedTextField(
            value=fullName,
            onValueChange = {
                fullName=it
            },
            label={
                Text("Full Name")
            },
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value=email,
            onValueChange = {
                email=it
            },
            label={
                Text("Email")
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value=phoneNumber,
            onValueChange = {
                phoneNumber=it
            },
            label={
                Text("Phone No.")
            },
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value=password,
            onValueChange = {
                password=it
            },
            label={
                Text("Password")
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value=confirmPassword,
            onValueChange = {
                confirmPassword=it
            },
            label={
                Text("Confirm Password")
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()

        )
        Spacer(modifier=Modifier.height(12.dp))

        TextButton(
            onClick = {
                userTypeExpanded=true
            },
            modifier=Modifier.fillMaxWidth()
        ){
            Text(
                text=if (userType.isEmpty()){
                    "Select user type"
                } else {
                    userType
                },
                fontSize=16.sp
            )
        }
        DropdownMenu(
            expanded=userTypeExpanded,
            onDismissRequest = {
                userTypeExpanded=false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text("Farmer")
                },
                onClick = {
                    userType = "Farmer"
                    userTypeExpanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text("Cooperative Society")
                },
                onClick = {
                    userType = "Cooperative Society"
                    userTypeExpanded = false
                }
            )

        }
        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick= {
                //Registration functionality
            },
            modifier = Modifier.fillMaxWidth()
            ){
            Text("REGISTER")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text="Already have an account?"
        )
        TextButton(
            onClick = onLoginClick
        ) {
            Text("Login")

        }
        Spacer(modifier = Modifier.height(20.dp))


    }

}
