package com.management.inventorypro.ui.theme.screens.tips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class AppFeatureTip(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(navController: NavController) {
    val appTips = listOf(
        AppFeatureTip(
            "Stay Logged In",
            "Tired of typing? Check 'Remember Me' on the login screen to skip the password prompt next time you open InventoryPro.",
            Icons.Default.Lock
        ),
        AppFeatureTip(
            "Custom Attributes",
            "Not every product is the same. Use 'Add Field' when creating an item to add unique info like Serial Numbers, Expiry Dates, or Batch IDs.",
            Icons.Default.AddCircle
        ),
        AppFeatureTip(
            "Safe Deletion",
            "Accidents happen! We've added a confirmation dialog to the delete button so you don't lose your data by a wrong tap.",
            Icons.Default.CheckCircle
        ),
        AppFeatureTip(
            "Visual Inventory",
            "Items with images are easier to find. Tap the 'Add Image' box to upload a photo from your gallery for any product.",
            Icons.Default.AccountBox
        ),
        AppFeatureTip(
            "Quick Navigation",
            "Tap any item in the 'View Inventory' list to jump straight into the Update screen and edit its details instantly.",
            Icons.Default.Send
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to Use InventoryPro") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "App Guide & Tricks",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red // Matching your Login Screen theme
                )
                Text(
                    text = "Get the most out of your management system with these quick features.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
            }

            items(appTips) { tip ->
                AppTipCard(tip)
            }
        }
    }
}

@Composable
fun AppTipCard(tip: AppFeatureTip) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = tip.icon,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = tip.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}