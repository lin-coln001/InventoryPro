package com.management.inventorypro.ui.theme.screens.dashboard

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.management.inventorypro.R
import com.management.inventorypro.data.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {
    val selectedItem = remember { mutableStateOf(0) }
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    // States for dynamic data
    var username by remember { mutableStateOf("User") }
    var itemCount by remember { mutableStateOf(0) }

    // Fetch data when the screen loads
// Inside DashboardScreen.kt
    LaunchedEffect(Unit) {
        // 1. Get Username
        authViewModel.getUsername {
            username = it
        }


        // 2. Get Current User UID for the correct path
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            // POINT TO THE USER'S SPECIFIC FOLDER
            val database = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("inventory")

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Now it counts only the items belonging to THIS user
                    itemCount = snapshot.childrenCount.toInt()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "InventoryPro") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White
                ),
                actions = {
                    Button(
                        onClick = { authViewModel.logout(navController, context) },
                        colors = ButtonDefaults.buttonColors(Color.Red)
                    ) {
                        Text(text = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.Blue) {
                NavigationBarItem(
                    selected = selectedItem.value == 0,
                    onClick = { selectedItem.value = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text(text = "Home") }
                )
                NavigationBarItem(
                    selected = selectedItem.value == 1,
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text(text = "Settings") }
                )
                NavigationBarItem(
                    selected = selectedItem.value == 2,
                    onClick = {navController.navigate("tips") },
                    icon = { Icon(Icons.Filled.Lightbulb, contentDescription = "Tips") },
                    label = { Text(text = "Tips") }
                )
                NavigationBarItem(
                    selected = selectedItem.value == 3,
                    onClick = { selectedItem.value = 3 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Person") },
                    label = { Text(text = "Person") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome back $username",
                fontSize = 25.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            AsyncImage(
                model = R.drawable.welcome_back, // Your GIF name
                imageLoader = imageLoader,     // Use the loader we just made
                contentDescription = "Dashboard Animation",
                modifier = Modifier
                    .size(140.dp)
                    .clip(RectangleShape),
//                .border(2.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )

            // Dynamic Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // THE LIVE INVENTORY CARD
                Card(
                    modifier = Modifier.size(110.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Blue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$itemCount", color = Color.White, fontSize = 28.sp)
                        Text(text = "Items", color = Color.White, fontSize = 16.sp)
                    }
                }

                // Placeholder Card 2 (e.g., Active Alerts)
                Card(
                    modifier = Modifier.size(110.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Blue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "0", color = Color.White, fontSize = 28.sp)
                        Text(text = "categories", color = Color.White, fontSize = 16.sp)
                    }
                }
            }

            // Action: Add New Item
            Card(
                onClick = { navController.navigate("add_product") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Inventory,
                        contentDescription = "Add Item",
                        tint = Color(0xFF004040),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Add New Item",
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(text = "Register a new product", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            // Action: View Inventory
            Card(
                onClick = {navController.navigate("view_inventory") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "View List",
                        tint = Color(0xFF004040),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "View Inventory",
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(text = "Check stock levels", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(navController = rememberNavController())
}