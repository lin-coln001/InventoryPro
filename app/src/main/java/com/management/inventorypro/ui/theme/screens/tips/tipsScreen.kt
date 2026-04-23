package com.management.inventorypro.ui.theme.screens.tips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// Consistency Palette
val DeepMidnight = Color(0xFF0A0E1A)
val SurfaceNavy = Color(0xFF161C2C)
val NeonCyan = Color(0xFF00E5FF)
val SoftCyan = Color(0xFFB2EBF2)

data class InventoryTip(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tips = listOf(
        InventoryTip("Folder Navigation", "Use the arrows next to category names to expand sections. A 'Down' arrow means the folder is open!", Icons.Default.Folder),
        InventoryTip("Smart Categories", "Consistency is key! Use the dropdown when adding items to keep your view organized.", Icons.Default.Category),
        InventoryTip("Custom Details", "Don't just stop at a name! Use 'Add Field' to track Serial Numbers or Expiry Dates.", Icons.Default.Extension),
        InventoryTip("Quick Updates", "Tap any product card to enter the Edit screen. You can change the photo or move categories.", Icons.Default.Edit),
        InventoryTip("System Sync", "All changes are updated in real-time across the cloud database for instant access.", Icons.Default.CloudSync)
    )

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = { Text("System Intelligence", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = NeonCyan
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceNavy,
                tonalElevation = 0.dp
            ) {
                val navItems = listOf(
                    Triple("dashboard", Icons.Filled.Home, "Home"),
                    Triple("settings", Icons.Filled.Settings, "Settings"),
                    Triple("tips", Icons.Filled.Lightbulb, "Tips"),
                    Triple("profile", Icons.Filled.Person, "Profile")
                )

                navItems.forEach { (route, icon, label) ->
                    val isSelected = currentRoute == route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = SoftCyan.copy(0.5f),
                            unselectedTextColor = SoftCyan.copy(0.5f),
                            indicatorColor = NeonCyan.copy(0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DeepMidnight)
        ) {
            Text(
                text = "Optimization Protocols",
                color = SoftCyan.copy(0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(tips) { tip ->
                    TipCard(tip)
                }
            }
        }
    }
}

@Composable
fun TipCard(tip: InventoryTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = NeonCyan.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = tip.icon,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = tip.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = SoftCyan.copy(alpha = 0.7f)
                )
            }
        }
    }
}