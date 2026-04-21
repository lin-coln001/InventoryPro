package com.management.inventorypro.ui.theme.screens.tips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class InventoryTip(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(navController: androidx.navigation.NavController) {
    val tips = listOf(
        InventoryTip(
            "Folder Navigation",
            "Use the arrows next to category names to expand or collapse sections. A 'Down' arrow means the folder is open!",
            Icons.Default.Lightbulb
        ),
        InventoryTip(
            "Smart Categories",
            "Consistency is key! Use the dropdown when adding items to stick to existing categories like 'Cars' or 'Electronics' to keep your view organized.",
            Icons.Default.Info
        ),
        InventoryTip(
            "Custom Details",
            "Don't just stop at a name! Use 'Add Field' to track specific data like Serial Numbers, Expiry Dates, or Color for each item.",
            Icons.Default.Lightbulb
        ),
        InventoryTip(
            "Quick Updates",
            "Tap any product card to enter the Edit screen. You can change the photo, move it to a different category, or delete it entirely.",
            Icons.Default.Info
        ),
        InventoryTip(
            "Image Placeholders",
            "If an item doesn't have a photo yet, the app will show a default placeholder. You can add a photo later by editing the product.",
            Icons.Default.Lightbulb
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Inventory Pro Tips") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tips) { tip ->
                TipCard(tip)
            }
        }
    }
}

@Composable
fun TipCard(tip: InventoryTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tip.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}