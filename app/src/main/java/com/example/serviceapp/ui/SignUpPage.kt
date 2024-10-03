package com.example.serviceapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.serviceapp.ui.theme.ServiceAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpPage(navController: NavController) {
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("+254") } // Prefill with +254
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var signUpStage by remember { mutableIntStateOf(1) }
    var signUpSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sign Up") }) },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (signUpSuccess) {
                    Text(text = "Sign up successful! You can sign in.", color = MaterialTheme.colorScheme.primary)
                }
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }

                when (signUpStage) {
                    1 -> {
                        TextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        TextField(
                            value = surname,
                            onValueChange = { surname = it },
                            label = { Text("Surname") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                            if (firstName.isNotEmpty() && surname.isNotEmpty()) {
                                signUpStage = 2
                            } else {
                                errorMessage = "First Name and Surname are required"
                            }
                        },
                            shape = RoundedCornerShape(16.dp), // Modify button shape here
                            colors = ButtonDefaults.buttonColors(
                                contentColorFor(backgroundColor = MaterialTheme.colorScheme.primaryContainer),
                                contentColor = MaterialTheme.colorScheme.inversePrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                            Text("Next")
                        }
                    }
                    2 -> {
                        TextField(
                            value = telephone,
                            onValueChange = { telephone = it },
                            label = { Text("Telephone (+254...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                            coroutineScope.launch {
                                checkPhoneRegistration(
                                    telephone,
                                    onSuccess = { signUpStage = 3 },
                                    onFailure = { errorMessage = it }
                                )
                            }
                        },
                            shape = RoundedCornerShape(16.dp), // Modify button shape here
                            colors = ButtonDefaults.buttonColors(
                                contentColorFor(backgroundColor = MaterialTheme.colorScheme.primaryContainer),
                                contentColor = MaterialTheme.colorScheme.inversePrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Check Number")
                        }
                    }
                    3 -> {
                        TextField(
                            value = pin,
                            onValueChange = { pin = it },
                            label = { Text("Enter PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        TextField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it },
                            label = { Text("Confirm PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                            if (pin == confirmPin) {
                                val hashedPin = hashPin(pin)
                                createNewUser(
                                    firstName,
                                    surname,
                                    telephone,
                                    hashedPin,
                                    onSuccess = {
                                        signUpSuccess = true
                                        coroutineScope.launch {
                                            delay(2000)  // Wait for 2 seconds
                                            navController.navigate("home")  // Navigate back to home
                                        }
                                    },
                                    onFailure = { errorMessage = it }
                                )
                            } else {
                                errorMessage = "PINs do not match"
                            }
                        },
                            shape = RoundedCornerShape(16.dp), // Modify button shape here
                            colors = ButtonDefaults.buttonColors(
                                contentColorFor(backgroundColor = MaterialTheme.colorScheme.primaryContainer),
                                contentColor = MaterialTheme.colorScheme.inversePrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                            Text("Complete Sign Up")
                        }
                    }
                }
            }
        }
    )
}

fun checkPhoneRegistration(telephone: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    FirebaseFirestore.getInstance().collection("users")
        .whereEqualTo("telephone", telephone)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                onFailure("User already exists. Please enter a different telephone number.")
            } else {
                onSuccess()
            }
        }
        .addOnFailureListener { onFailure("Failed to check telephone registration. Please try again.") }
}

fun createNewUser(
    firstName: String,
    surname: String,
    telephone: String,
    pin: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userEmail = "$telephone@example.com"

    auth.createUserWithEmailAndPassword(userEmail, "dummyPassword")
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                val user = hashMapOf(
                    "firstName" to firstName,
                    "surname" to surname,
                    "telephone" to telephone,
                    "pin" to pin,
                    "isAdmin" to false // Set default admin role to false
                )
                firestore.collection("users").document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure("Failed to create user: ${e.message}")
                    }
            } else {
                task.exception?.message?.let { onFailure(it) }
            }
        }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSignUpPage() {
    ServiceAppTheme {
        // Preview the sign-in page with a mock NavController
        SignUpPage(navController = rememberNavController())
    }
}
