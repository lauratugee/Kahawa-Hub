
package com.example.kahawahub

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private val ProfileBrown = Color(0xFF684832)
private val ProfileGreen = Color(0xFF245640)
private val ProfileCream = Color(0xFFF7F3E9)

@Composable
fun CooperativeProfilePage(
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Each cooperative uses its own authenticated account ID.
    val cooperativeId = auth.currentUser?.uid

    var currentSection by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var cooperativeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var county by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var services by remember { mutableStateOf("") }

    var requirements by remember { mutableStateOf("") }
    var membershipFees by remember { mutableStateOf("") }
    var benefits by remember { mutableStateOf("") }
    var joiningProcedure by remember { mutableStateOf("") }
    var documentsRequired by remember { mutableStateOf("") }
    var membershipContact by remember { mutableStateOf("") }

    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var officeAddress by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var openingHours by remember { mutableStateOf("") }

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Load only this logged-in cooperative's document.
    LaunchedEffect(cooperativeId) {
        if (cooperativeId == null) {
            isLoading = false
        } else {
            db.collection("cooperatives")
                .document(cooperativeId)
                .get()
                .addOnSuccessListener { document ->
                    cooperativeName =
                        document.getString("name") ?: ""

                    description =
                        document.getString("description") ?: ""

                    county =
                        document.getString("county") ?: ""

                    location =
                        document.getString("location") ?: ""

                    services =
                        (document.get("services") as? List<*>)
                            ?.joinToString("\n") ?: ""

                    requirements =
                        document.getString("membership_requirements") ?: ""

                    membershipFees =
                        document.getString("membership_fees") ?: ""

                    benefits =
                        document.getString("membership_benefits") ?: ""

                    joiningProcedure =
                        document.getString("joining_procedure") ?: ""

                    documentsRequired =
                        document.getString("documents_required") ?: ""

                    membershipContact =
                        document.getString("membership_contact") ?: ""

                    phone =
                        document.getString("phone") ?: ""

                    email =
                        document.getString("email") ?: ""

                    officeAddress =
                        document.getString("office_address") ?: ""

                    contactPerson =
                        document.getString("contact_person") ?: ""

                    openingHours =
                        document.getString("opening_hours") ?: ""

                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(
                        context,
                        "Could not load cooperative profile",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    fun saveProfile(data: Map<String, Any>) {
        if (cooperativeId == null) {
            Toast.makeText(
                context,
                "Please log in again",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // merge() preserves other sections already saved in this document.
        db.collection("cooperatives")
            .document(cooperativeId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Information saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                currentSection = null
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Could not save information: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (currentSection == null) {

            Text(
                text = "Cooperative Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileBrown
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Manage the information farmers see about your cooperative.",
                color = Color.DarkGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator(color = ProfileGreen)
            } else {
                ProfileSectionCard(
                    title = "Basic Information",
                    description = "Cooperative name, description and location",
                    onClick = { currentSection = "basic" }
                )

                ProfileSectionCard(
                    title = "Services Offered",
                    description = "Add and manage the services your cooperative provides",
                    onClick = { currentSection = "services" }
                )

                ProfileSectionCard(
                    title = "Membership Information",
                    description = "Requirements, fees, benefits and joining procedure",
                    onClick = { currentSection = "membership" }
                )

                ProfileSectionCard(
                    title = "Contact Information",
                    description = "Phone number, email and office details",
                    onClick = { currentSection = "contact" }
                )

                ProfileSectionCard(
                    title = "Account Settings",
                    description = "Account details, password and logout",
                    onClick = { currentSection = "account" }
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Logout")
                }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showLogoutDialog = false
                        },
                        title = { Text("Logout") },
                        text = {
                            Text("Are you sure you want to logout?")
                        },
                        confirmButton = {
                            TextButton(onClick = onLogoutClick) {
                                Text("Logout", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showLogoutDialog = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

        } else {
            Text(
                text = when (currentSection) {
                    "basic" -> "Basic Information"
                    "services" -> "Services Offered"
                    "membership" -> "Membership Information"
                    "contact" -> "Contact Information"
                    else -> "Account Settings"
                },
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileBrown
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { currentSection = null }) {
                Text("← Back to Profile", color = ProfileGreen)
            }

            when (currentSection) {
                "basic" -> {
                    ProfileInput(
                        "Cooperative Name",
                        cooperativeName
                    ) { cooperativeName = it }

                    ProfileInput(
                        "Description",
                        description,
                        minLines = 4
                    ) { description = it }

                    ProfileInput("County", county) { county = it }

                    ProfileInput(
                        "Physical Location",
                        location
                    ) { location = it }

                    ProfileSaveButton {
                        if (cooperativeName.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please enter the cooperative name",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            saveProfile(
                                mapOf(
                                    "name" to cooperativeName.trim(),
                                    "description" to description.trim(),
                                    "county" to county.trim(),
                                    "location" to location.trim()
                                )
                            )
                        }
                    }
                }

                "services" -> {
                    Text(
                        "Enter one service per line.",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ProfileInput(
                        "Services Offered",
                        services,
                        minLines = 7,
                        placeholder = "e.g.\nCoffee collection\nFarmer training\nFarm inputs"
                    ) { services = it }

                    ProfileSaveButton {
                        val serviceList = services
                            .lines()
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        saveProfile(
                            mapOf("services" to serviceList)
                        )
                    }
                }

                "membership" -> {
                    ProfileInput(
                        "Membership Requirements",
                        requirements,
                        minLines = 3
                    ) { requirements = it }

                    ProfileInput(
                        "Membership Fees",
                        membershipFees
                    ) { membershipFees = it }

                    ProfileInput(
                        "Membership Benefits",
                        benefits,
                        minLines = 3
                    ) { benefits = it }

                    ProfileInput(
                        "Joining Procedure",
                        joiningProcedure,
                        minLines = 3
                    ) { joiningProcedure = it }

                    ProfileInput(
                        "Documents Required",
                        documentsRequired,
                        minLines = 3
                    ) { documentsRequired = it }

                    ProfileInput(
                        "Membership Contact Person",
                        membershipContact
                    ) { membershipContact = it }

                    ProfileSaveButton {
                        saveProfile(
                            mapOf(
                                "membership_requirements" to requirements.trim(),
                                "membership_fees" to membershipFees.trim(),
                                "membership_benefits" to benefits.trim(),
                                "joining_procedure" to joiningProcedure.trim(),
                                "documents_required" to documentsRequired.trim(),
                                "membership_contact" to membershipContact.trim()
                            )
                        )
                    }
                }

                "contact" -> {
                    ProfileInput(
                        "Official Phone Number",
                        phone
                    ) { phone = it }

                    ProfileInput(
                        "Official Email Address",
                        email
                    ) { email = it }

                    ProfileInput(
                        "Office Address",
                        officeAddress,
                        minLines = 2
                    ) { officeAddress = it }

                    ProfileInput(
                        "Contact Person",
                        contactPerson
                    ) { contactPerson = it }

                    ProfileInput(
                        "Opening Hours",
                        openingHours
                    ) { openingHours = it }

                    ProfileSaveButton {
                        saveProfile(
                            mapOf(
                                "phone" to phone.trim(),
                                "email" to email.trim(),
                                "office_address" to officeAddress.trim(),
                                "contact_person" to contactPerson.trim(),
                                "opening_hours" to openingHours.trim()
                            )
                        )
                    }
                }

                "account" -> {
                    Text(
                        text = "Account Email",
                        fontWeight = FontWeight.SemiBold,
                        color = ProfileBrown
                    )

                    Text(
                        text = auth.currentUser?.email
                            ?: "Email unavailable",
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val accountEmail = auth.currentUser?.email

                            if (accountEmail.isNullOrBlank()) {
                                Toast.makeText(
                                    context,
                                    "Account email is unavailable",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                auth.sendPasswordResetEmail(accountEmail)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Password reset email sent",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(
                                            context,
                                            "Could not send reset email: ${exception.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileGreen
                        )
                    ) {
                        Text("Send Password Reset Email")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout")
                    }

                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showLogoutDialog = false
                            },
                            title = { Text("Logout") },
                            text = {
                                Text("Are you sure you want to logout?")
                            },
                            confirmButton = {
                                TextButton(onClick = onLogoutClick) {
                                    Text("Logout", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showLogoutDialog = false }
                                ) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ProfileBrown
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Edit information →",
                fontSize = 13.sp,
                color = ProfileGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ProfileInput(
    label: String,
    value: String,
    minLines: Int = 1,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = {
            if (placeholder.isNotBlank()) {
                Text(placeholder)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        minLines = minLines,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun ProfileSaveButton(
    onClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ProfileGreen
        )
    ) {
        Text("Save Changes")
    }
}

