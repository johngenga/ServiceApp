package com.example.serviceapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.serviceapp.ui.theme.ServiceAppTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetails(
    serviceName: String,
    userName: String,
    userTelephone: String,
    navController: NavHostController
) {
    var serviceRequest by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Service Details") }) },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Welcome $userName", style = MaterialTheme.typography.titleLarge)
                Text("You have selected $serviceName", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "We will get in touch via $userTelephone. Please describe your request.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(28.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // TextField for service request input
                TextField(
                    value = serviceRequest,
                    onValueChange = { serviceRequest = it },
                    label = { Text("Describe your request") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (serviceRequest.isNotBlank()) {
                            isSubmitting = true
                            submitServiceRequest(
                                serviceName,
                                userName,
                                userTelephone,
                                serviceRequest,
                                onSuccess = {
                                    snackbarMessage = "Request submitted successfully!"
                                    showSnackbar = true
                                    isSubmitting = false
                                    // After a short delay, navigate to the requests screen
                                    navController.navigate("userRequests/$userName/$userTelephone")
                                },
                                onFailure = {
                                    snackbarMessage = "Failed to submit request. Please try again."
                                    showSnackbar = true
                                    isSubmitting = false
                                }
                                // Pass navController here
                            )
                        } else {
                            snackbarMessage = "Please describe your request before submitting."
                            showSnackbar = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting // Disable button while submitting
                ) {
                    Text(if (isSubmitting) "Submitting..." else "Submit Request")
                }

                // Snackbar for submission feedback
                if (showSnackbar) {
                    Snackbar(
                        action = {
                            TextButton(onClick = { showSnackbar = false }) {
                                Text("Dismiss")
                            }
                        },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(snackbarMessage)
                    }
                }
            }
        }
    )
}

// Function to submit the service request to Firestore
private fun submitServiceRequest(
    serviceName: String,
    userName: String,
    userTelephone: String,
    serviceRequest: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val serviceData = hashMapOf(
        "serviceName" to serviceName,
        "userName" to userName,
        "userTelephone" to userTelephone,
        "serviceRequest" to serviceRequest,
        "timestamp" to FieldValue.serverTimestamp(),
        "status" to "Pending" // Initial status
    )

    db.collection("serviceRequests")
        .add(serviceData)
        .addOnSuccessListener {
            onSuccess() // Call success callback
        }
        .addOnFailureListener { onFailure() }
}


// Preview function for ServiceDetails
@Preview(showBackground = true)
@Composable
private fun PreviewServiceDetails() {
    ServiceAppTheme {
        ServiceDetails(
            serviceName = "String",
            userName = "John Doe",
            userTelephone = "+254712345678",
            navController = rememberNavController()
        )
    }
}