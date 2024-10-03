package com.example.serviceapp.ui

// Data class for RequestCard
data class RequestCardData(
    val id: String, // Document ID for Firestore updates
    val serviceName: String,
    val serviceRequest: String,
    val userName: String,
    val userTelephone: String,
    val status: String
)