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

    val isSystemOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val themeColor = if (isSystemOnline) NeonCyan else DangerRed

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val tips = listOf(
        InventoryTip(
            "Universal Deep Search",
            "The search bar scans everything. Type a name, a category, or even data hidden inside your custom fields to find items instantly.",
            Icons.Default.Search
        ),
        InventoryTip(
            "Custom Field Logic",
            "Your system has no fixed variables. Use 'Custom Fields' to track Prices, Serial Numbers, or Locations based on your specific needs.",
            Icons.Default.Tune
        ),
        InventoryTip(
            "Visual Status Uplink",
            "Cyan means you are online and syncing. If the system turns Red, you are in Offline Mode—data is currently Read-Only.",
            Icons.Default.WifiTethering
        ),
        InventoryTip(
            "Neon Highlighting",
            "When searching, the system will highlight matches in Neon Cyan so you can visually verify why an item was retrieved.",
            Icons.Default.AutoFixHigh
        ),
        InventoryTip(
            "Dynamic Hierarchy",
            "Items are grouped by 'Parent > Sub-category'. Use the arrows to drill down into specific sections of your database.",
            Icons.Default.AccountTree
        )
    )

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "System Intelligence",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = themeColor
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
                        icon = { Icon(icon, null, tint = if (isSelected) themeColor else unselectedColor) },
                        label = { Text(label, color = if (isSelected) themeColor else unselectedColor) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = themeColor.copy(0.1f))
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
                text = "Operational Protocols",
                color = if (isSystemOnline) themeColor.copy(0.6f) else DangerRed.copy(0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(tips) { tip ->
                    TipCard(tip, themeColor)
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