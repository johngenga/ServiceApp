package com.example.serviceapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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


// Composable for showing the list of user requests
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRequestsScreen(
    userName: String,
    userTelephone: String,
    navController: NavController
) {

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val (requests, setRequests) = remember { mutableStateOf<List<RequestCardData>>(emptyList()) }
    val db = FirebaseFirestore.getInstance()

    // Fetch requests for the user from Firebase
    LaunchedEffect(Unit) {
        isLoading = true
        db.collection("requests")
            .whereEqualTo("userName", userName)
            .whereEqualTo("userTelephone", userTelephone)
            .get()
            .addOnSuccessListener { result ->
                val fetchedRequests = result.map { document ->
                    RequestCardData(
                        id = document.id,
                        serviceName = document.getString("serviceName") ?: "",
                        serviceRequest = document.getString("serviceRequest") ?: "",
                        userName = document.getString("userName") ?: "",
                        userTelephone = document.getString("userTelephone") ?: "",
                        status = document.getString("status") ?: ""
                    )
                }
                setRequests(fetchedRequests)
                isLoading = false
            }
            .addOnFailureListener {
                errorMessage = "Error fetching requests."
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Your Requests") })
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    errorMessage != null -> {
                        Text(text = errorMessage ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                    }
                    requests.isEmpty() -> {
                        Text(text = "No requests found.", style = MaterialTheme.typography.bodyMedium)
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(requests) { request ->
                                RequestCard(
                                    request = request,
                                    onCancelClick = { cancelRequest(request, db) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        // Buttons to go back or logout
                        Button(
                            onClick = { navController.navigate("serviceMenu") },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Back to Service Menu")
                        }
                    }
                }
            }
        }
    )
}

// Composable for each request card
@Composable
fun RequestCard(request: RequestCardData, onCancelClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = request.serviceName, style = MaterialTheme.typography.titleMedium)
            Text(text = request.serviceRequest, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${request.status}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onCancelClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cancel Request")
            }
        }
    }
}

// Function to cancel a request by removing it from Firestore
private fun cancelRequest(request: RequestCardData, db: FirebaseFirestore) {
    db.collection("requests")
        .whereEqualTo("serviceName", request.serviceName)
        .whereEqualTo("serviceRequest", request.serviceRequest)
        .whereEqualTo("status", request.status)
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                db.collection("requests").document(document.id).delete()
            }
        }
        .addOnFailureListener {
            // Handle the error (e.g., show an error message)
        }
}
