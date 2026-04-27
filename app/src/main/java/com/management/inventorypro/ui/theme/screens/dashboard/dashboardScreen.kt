package com.management.inventorypro.ui.theme.screens.dashboard

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.management.inventorypro.R
import com.management.inventorypro.data.AuthViewModel
import com.management.inventorypro.ui.theme.DangerRed
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.OffWhite
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.ui.theme.screens.dashboard.ActionCard

// --- CUSTOM NEON COLORS ---
//val DeepMidnight = Color(0xFF0A0E1A)   // Background
//val SurfaceNavy = Color(0xFF161C2C)    // Card Surface
//val NeonCyan = Color(0xFF00E5FF)       // Primary Glow
//val SoftCyan = Color(0xFFB2EBF2)       // Text Secondary
//val DangerRed = Color(0xFFFF5252)      // Logout/Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var username by remember { mutableStateOf("User") }
    var itemCount by remember { mutableStateOf(0) }

    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
        }.build()

    LaunchedEffect(Unit) {
        authViewModel.getUsername { username = it }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val database = FirebaseDatabase.getInstance().getReference("users").child(uid).child("inventory")
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    itemCount = snapshot.childrenCount.toInt()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    Scaffold(
        containerColor = DeepMidnight, // Ensures the background is deep navy
        topBar = {
            TopAppBar(
                title = {
                    Text("InventoryPro", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = NeonCyan
                ),
                actions = {
                    TextButton(
                        onClick = { authViewModel.logout(navController, context) },
                        colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                    ) {
                        Text("LOGOUT", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceNavy,
                tonalElevation = 0.dp
            ) {
                // Define your destinations clearly
                val navItems = listOf(
                    // CHANGE "home" TO "dashboard"
                    Triple("dashboard", Icons.Filled.Home, "Home"),
                    Triple("settings", Icons.Filled.Settings, "Settings"),
                    Triple("tips", Icons.Filled.Lightbulb, "Tips"),
                    Triple("profile", Icons.Filled.Person, "Profile")
                )

                navItems.forEach { (route, icon, label) ->
                    // This is the key: compare currentRoute with the target route
                    val isSelected = currentRoute == route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            // CRASH PREVENTER: Only navigate if we aren't already there
                            if (!isSelected) {
                                navController.navigate(route) {
                                    // This pops up to the start destination to avoid
                                    // building up a massive stack of screens
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                // Make the icon "glow" more when selected
                                tint = if (isSelected) NeonCyan else SoftCyan.copy(alpha = 0.5f)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (isSelected) NeonCyan else SoftCyan.copy(alpha = 0.5f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            // The "pill" behind the icon
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(DeepMidnight)
                .padding(16.dp)
        ) {
            // Welcome, Header
            Text(
                text = if (username == "...") "INITIALIZING..." else "Welcome back $username",
                color = if (username == "...") SoftCyan.copy(0.3f) else OffWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
            // Dynamic Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(value = "$itemCount", label = "Items", modifier = Modifier.weight(1f))
                StatCard(value = "Live", label = "Status", modifier = Modifier.weight(1f))
            }

            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.labelLarge,
                color = SoftCyan.copy(0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Action: Add New Item
            ActionCard(
                title = "Add New Item",
                subtitle = "Register product to database",
                icon = Icons.Filled.Inventory,
                onClick = { navController.navigate("add_product") }
            )

            // Action: View Inventory
            ActionCard(
                title = "View Blickies",
                subtitle = "Check current stock levels",
                icon = Icons.Filled.List,
                onClick = { navController.navigate("view_inventory") }
            )
        }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = NeonCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = SoftCyan.copy(0.7f), fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 12.sp, color = SoftCyan.copy(0.6f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = SoftCyan.copy(0.3f), modifier = Modifier.size(16.dp))
        }
    }
}