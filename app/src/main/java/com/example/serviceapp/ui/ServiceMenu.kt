package com.example.serviceapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.serviceapp.R
import com.example.serviceapp.ui.theme.Pink40
import com.example.serviceapp.ui.theme.ServiceAppTheme

data class Service(
    val name: String,
    val icon: Int // Resource ID for the icon
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceMenu(navController: NavController, userName: String, userTelephone: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {}
    val services = listOf(
        Service("Financial Advice", R.drawable.ic_financial_advice),
        Service("Inventory Management", R.drawable.ic_inventory_mgt),
        Service("Storage", R.drawable.ic_warehouse),
        Service("Website Creation", R.drawable.ic_website_design),
        Service("Design and Tailoring", R.drawable.ic_clothing),
        Service("Construction", R.drawable.ic_construction),
        Service("Electrical Work", R.drawable.ic_electrical),
        Service("Landscaping", R.drawable.ic_landscaping),
        Service("Internet Service", R.drawable.ic_internet_service),
        Service("Carpentry Service", R.drawable.ic_carpenter),
        Service("Car Rental", R.drawable.ic_car_rental),
        Service("Private Tuition", R.drawable.ic_tuition),
        Service("Accommodation", R.drawable.ic_bed_and_breakfast),

    )
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Service Menu") })
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.inversePrimary)
                        .fillMaxSize()
                        .weight(1f), // Ensures the service grid doesn't push other elements off the screen
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome, $userName",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "Your phone number: $userTelephone",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp), // Makes it adaptive to screen size
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()

                    ) {
                        items(services.size) { index ->
                            val service = services[index]
                            ServiceIconButton(service = service) {
                                // Navigate to service details page, passing service and user details
                                navController.navigate("ServiceDetails/${service.name}/$userName/$userTelephone")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ServiceIconButton(service: Service, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        IconButton(onClick = onClick) {
            Image(
                imageVector = ImageVector.vectorResource(id = service.icon),
                contentDescription = service.name,
                modifier = Modifier
                    .size(150.dp)
            )
        }
        Text(
            text = service.name,
            style = MaterialTheme.typography.labelSmall
        )

    }
}

// Preview function for ServiceMenu
@Preview(showBackground = true)
@Composable
private fun PreviewServiceMenu() {
    ServiceAppTheme {
        ServiceMenu(
            navController = rememberNavController(),
            userName = "John Doe",
            userTelephone = "+254712345678"
        )
    }
}
