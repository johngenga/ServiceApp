package com.example.serviceapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore


// Admin Dashboard Screen
@Composable
fun AdminDashboardScreen(navController: NavController) {
    val (requests, setRequests) = remember { mutableStateOf<List<RequestCardData>>(emptyList()) }
    val db = FirebaseFirestore.getInstance() // Firestore instance

    // Fetch all requests from Firestore
    LaunchedEffect(Unit) {
        db.collection("serviceRequests")
            .get()
            .addOnSuccessListener { result ->
                val fetchedRequests = result.map { document ->
                    RequestCardData(
                        id = document.id,  // Document ID for updating
                        serviceName = document.getString("serviceName") ?: "",
                        serviceRequest = document.getString("serviceRequest") ?: "",
                        userName = document.getString("userName") ?: "",
                        userTelephone = document.getString("userTelephone") ?: "",
                        status = document.getString("status") ?: ""
                    )
                }
                setRequests(fetchedRequests)
            }
            .addOnFailureListener {
                // Handle error fetching requests
            }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        // Display all requests
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(requests) { request ->
                AdminRequestCard(requestData = request) { newStatus ->
                    // Pass the db instance to the updateRequestStatus function
                    updateRequestStatus(request, newStatus, db)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Logout or navigate back buttons for the admin
        Button(
            onClick = { navController.navigate("signIn") },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Log Out")
        }
    }
}

// Function to update request status in Firestore
fun updateRequestStatus(request: RequestCardData, newStatus: String, db: FirebaseFirestore) {
    db.collection("serviceRequests")
        .document(request.id)
        .update("status", newStatus)
        .addOnSuccessListener {
            // Optionally show a success message
        }
        .addOnFailureListener {
            // Handle failure
        }
}


// Admin Request Card
@Composable
fun AdminRequestCard(requestData: RequestCardData, onStatusChange: (String) -> Unit) {
    var selectedStatus by remember { mutableStateOf(requestData.status) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Service: ${requestData.serviceName}")
            Text("Request: ${requestData.serviceRequest}")
            Text("User: ${requestData.userName} (${requestData.userTelephone})")
            Text("Current Status: ${requestData.status}")

            // Radio Buttons for status selection
            StatusRadioButtons(selectedStatus) { newStatus ->
                selectedStatus = newStatus
                onStatusChange(newStatus) // Notify parent about status change
            }
        }
    }
}

// Radio Buttons for selecting status
@Composable
fun StatusRadioButtons(selectedStatus: String, onStatusSelected: (String) -> Unit) {
    val statusOptions = listOf("Pending", "Received", "In Progress", "Complete", "Canceled")

    Column {
        statusOptions.forEach { status ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = (selectedStatus == status),
                    onClick = { onStatusSelected(status) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = status)
            }
        }
    }
}

