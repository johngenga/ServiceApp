package com.example.serviceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.serviceapp.ui.AdminDashboardScreen
import com.example.serviceapp.ui.HomePage
import com.example.serviceapp.ui.ServiceDetails
import com.example.serviceapp.ui.SignUpPage
import com.example.serviceapp.ui.SignInPage
import com.example.serviceapp.ui.ServiceMenu
import com.example.serviceapp.ui.UserRequestsScreen
import com.example.serviceapp.ui.theme.ServiceAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.serviceapp.ui.RequestCardData


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        // Get Firebase Auth instance
        val firebaseAuth = FirebaseAuth.getInstance()
        // Check if the user is signed in
        val currentUser = firebaseAuth.currentUser
        setContent {
            ServiceAppTheme {
                val navController = rememberNavController()
                if (currentUser != null) {
                    // User is signed in, navigate to the service menu or home page
                    AppScaffold(navController = navController, startDestination = "serviceMenu")
                } else {
                    // User is not signed in, navigate to the sign-in page
                    AppScaffold(navController = navController, startDestination = "home")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(navController: NavHostController, startDestination: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "ServiceApp",
                    color = Color.Black
                ) },
                actions = {
                    IconButton(onClick = { /* Handle menu click */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
            )
        },
        content = { innerPadding ->
            // Apply padding to the Column, not AppNavHost to avoid overlapping
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding), // padding applied here
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("signIn") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp) // button styling
                ) {
                    Text("Sign In")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("signUp") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp) // button styling
                ) {
                    Text("Sign Up")
                }
            }
            AppNavHost(navController = navController, startDestination = startDestination) // Place AppNavHost here
        }
    )
}

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("home") { HomePage(navController) }
        composable("signIn") { SignInPage(navController = navController) }
        composable("signUp") { SignUpPage(navController = navController) }
        composable("adminDashboard") { AdminDashboardScreen(navController) }

        // ServiceMenu now accepts parameters in the route
        // Use navArguments to pass userName and userTelephone to ServiceMenu
        composable(
            route = "serviceMenu/{userName}/{userTelephone}",
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType },
                navArgument("userTelephone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userTelephone = backStackEntry.arguments?.getString("userTelephone") ?: ""
            ServiceMenu(navController, userName, userTelephone)
        }
        // ServiceDetails page route with arguments
        composable(
            route = "serviceDetails/{serviceName}/{userName}/{userTelephone}",
            arguments = listOf(
                navArgument("serviceName") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType },
                navArgument("userTelephone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Extract the arguments from the navigation
            val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userTelephone = backStackEntry.arguments?.getString("userTelephone") ?: ""

            // Navigate to ServiceDetails screen
            ServiceDetails(serviceName = serviceName, userName = userName, userTelephone = userTelephone, navController = navController)
        }
        composable("userRequests/{userName}/{userTelephone}") { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val userTelephone = backStackEntry.arguments?.getString("userTelephone") ?: ""

            var requests by remember { mutableStateOf<List<RequestCardData>>(emptyList()) }
            val db = FirebaseFirestore.getInstance()

            LaunchedEffect(userTelephone) {
                db.collection("requests")
                    .whereEqualTo("userTelephone", userTelephone)
                    .get()
                    .addOnSuccessListener { result ->
                        val fetchedRequests = result.documents.map { document ->
                            RequestCardData(
                                id = document.id,
                                serviceName = document.getString("serviceName") ?: "",
                                serviceRequest = document.getString("serviceRequest") ?: "",
                                userName = document.getString("userName") ?: "",
                                userTelephone = document.getString("userTelephone") ?: "",
                                status = document.getString("status") ?: "Pending"
                            )
                        }
                        requests = fetchedRequests
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
            }
            // Pass the fetched requests list to UserRequestsScreen
            UserRequestsScreen(userName, userTelephone, navController)
        }


    }}}
