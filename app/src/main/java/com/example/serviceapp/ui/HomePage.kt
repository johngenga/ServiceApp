package com.example.serviceapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.serviceapp.ui.theme.ServiceAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home") })
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Welcome to the Service App!",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .size(300.dp)
                        .padding(8.dp)
                        .fillMaxWidth()
                )
                Text(
                    text = "We give you personalized access to " +
                            "highly skilled service providers for " +
                            "high quality results. Sing Up today to get connected ",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(28.dp)
                )
                Button(
                    onClick = { navController.navigate("signIn") },
                    shape = RoundedCornerShape(16.dp), // Modify button shape here
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Sign In")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("signUp") },
                    shape = RoundedCornerShape(16.dp), // Modify button shape here
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Sign Up")
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewHomePage() {
    ServiceAppTheme {
        HomePage(navController = rememberNavController())
    }
}
