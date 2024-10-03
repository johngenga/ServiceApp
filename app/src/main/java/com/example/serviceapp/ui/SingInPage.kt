package com.example.serviceapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import com.example.serviceapp.ui.theme.ServiceAppTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.checkerframework.checker.units.qual.C

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInPage(navController: NavHostController) {
    var telephone by remember { mutableStateOf("+254") }
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sign In") }) },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }

                TextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Telephone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Enter PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            signInWithPin(telephone, pin,
                                onSuccess = { userName, userTelephone, isAdmin ->
                                    // Handle successful login and navigate to the correct page based on role
                                    errorMessage = "Login successful!"

                                    if (isAdmin) {
                                        // Navigate to admin dashboard if user is an admin
                                        navController.navigate("adminDashboard")
                                    } else {
                                        // Navigate to regular user service menu
                                        navController.navigate("serviceMenu/$userName/$userTelephone")
                                    }
                                },
                                onFailure = { errorMessage = it }
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        contentColorFor(backgroundColor = MaterialTheme.colorScheme.primaryContainer),
                        contentColor = MaterialTheme.colorScheme.inversePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Sign In")
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Forgot PIN option
                TextButton(onClick = {
                    // Handle Forgot PIN logic
                    coroutineScope.launch {
                        resetPinAndSendSms(telephone, onFailure = { errorMessage = it })
                        navController.navigate("signInPage") // Navigate back to sign in
                    }
                }) {
                    Text("Forgot PIN?")
                }
            }
        }
    )
}

// Function to handle user sign-in with PIN
fun signInWithPin(
    telephone: String,
    pin: String,
    onSuccess: (String, String, Boolean) -> Unit,  // Callback for successful login with isAdmin
    onFailure: (String) -> Unit  // Callback for failed login
) {
    // Get the Firestore instance
    val firestore = FirebaseFirestore.getInstance()

    // Query the 'users' collection where 'telephone' matches the input
    firestore.collection("users")
        .whereEqualTo("telephone", telephone)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                // No user found with the given telephone number
                onFailure("User not found. Please check your telephone number.")
            } else {
                val document = querySnapshot.documents[0]
                val storedPin = document.getString("pin")  // Retrieve the stored hashed pin

                if (storedPin == hashPin(pin)) {
                    // PIN matches, retrieve user details
                    val userName = "${document.getString("firstName")} ${document.getString("surname")}"
                    val userTelephone = document.getString("telephone") ?: ""
                    val isAdmin = document.getBoolean("isAdmin") ?: false  // Check if the user is an admin

                    // Call success callback with user details and isAdmin status
                    onSuccess(userName, userTelephone, isAdmin)
                } else {
                    // PIN does not match
                    onFailure("Incorrect PIN. Please try again.")
                }
            }
        }
        .addOnFailureListener { e ->
            // Failed to query Firestore
            onFailure("Sign-in failed. Please try again later. Error: ${e.message}")
        }
}

// Function to reset the user's PIN and send it via SMS
fun resetPinAndSendSms(telephone: String, onFailure: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    // Generate a new random 4-digit PIN
    val newPin = generateRandomPin()

    // Update the user's PIN in Firestore
    firestore.collection("users")
        .whereEqualTo("telephone", telephone)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                onFailure("User not found. Please check your telephone number.")
            } else {
                val document = querySnapshot.documents[0]
                val userId = document.id

                // Update the user's PIN
                firestore.collection("users").document(userId)
                    .update("pin", hashPin(newPin))  // Store hashed PIN
                    .addOnSuccessListener {
                        // Send the new PIN via SMS
                        sendPinSms(telephone, newPin)
                    }
                    .addOnFailureListener { e ->
                        onFailure("Failed to reset PIN. Error: ${e.message}")
                    }
            }
        }
        .addOnFailureListener { e ->
            onFailure("Failed to reset PIN. Error: ${e.message}")
        }
}

// Function to generate a random 4-digit PIN
fun generateRandomPin(): String {
    return (1000..9999).random().toString()
}

// Mock function to simulate sending SMS
fun sendPinSms(telephone: String, newPin: String) {
    // This is where you would integrate with an SMS service provider like Twilio
    println("Sending SMS to $telephone with new PIN: $newPin")
}
@Preview(showBackground = true)
@Composable
private fun PreviewSignInPage() {
    ServiceAppTheme {
        // Preview the sign-in page with a mock NavController
        SignInPage(navController = rememberNavController())
    }
}
