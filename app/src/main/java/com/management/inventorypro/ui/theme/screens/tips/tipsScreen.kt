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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.management.inventorypro.ui.theme.DangerRed
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.util.ConnectivityObserver


data class InventoryTip(
    val title: String,
    val description: String,
    val icon: ImageVector
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(navController: NavController) {
    val context = LocalContext.current
    val connectivityObserver = remember { ConnectivityObserver(context) }

    // --- OFFLINE SHIFT LOGIC ---
    val isSystemOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val themeColor = if (isSystemOnline) NeonCyan else DangerRed

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
                    titleContentColor = themeColor // Shifted
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
                    val unselectedColor = if (isSystemOnline) SoftCyan.copy(0.5f) else DangerRed.copy(0.3f)

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
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (isSelected) themeColor else unselectedColor
                            )
                        },
                        label = {
                            Text(
                                label,
                                color = if (isSelected) themeColor else unselectedColor
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = themeColor.copy(0.1f)
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
                color = if (isSystemOnline) SoftCyan.copy(0.6f) else DangerRed.copy(0.6f), // Shifted
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(tips) { tip ->
                    TipCard(tip, themeColor) // Pass themeColor to card
                }
            }
        }
    }
}

@Composable
fun TipCard(tip: InventoryTip, themeColor: Color) {
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
                color = themeColor.copy(alpha = 0.1f), // Shifted
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = tip.icon,
                    contentDescription = null,
                    tint = themeColor, // Shifted
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