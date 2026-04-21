package com.management.inventorypro.ui.theme.screens.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.data.InventoryViewModel
import com.management.inventorypro.models.ProductModel
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewInventoryScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    // Path: users/[UID]/inventory
    val database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("inventory")

    val productList = remember { mutableStateListOf<ProductModel>() }
    var isLoading by remember { mutableStateOf(true) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // --- FETCH DATA ---
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnap in snapshot.children) {
                    val product = productSnap.getValue(ProductModel::class.java)
                    if (product != null) {
                        // If the category field is missing in DB, it will use
                        // the default value from your ProductModel ("Uncategorized")
                        productList.add(product)
                    }
                }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- GROUPING LOGIC ---
    // This happens reactively whenever productList changes
    val groupedItems = productList.groupBy { it.category.ifEmpty { "Uncategorized" } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Inventory Management") }) }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (productList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items found. Add some in the Dashboard!")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                groupedItems.forEach { (categoryName, items) ->
                    item {
                        CategoryCard(
                            name = categoryName,
                            itemCount = items.size,
                            isExpanded = expandedCategory == categoryName,
                            onToggle = { expandedCategory = if (expandedCategory == categoryName) null else categoryName },
                            items = items,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: ProductModel, maxFields: Int, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Product Details
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)

                // Show limited fields based on settings
                val fieldsToShow = if (maxFields == 0) {
                    product.customFields.toList()
                } else {
                    product.customFields.toList().take(maxFields)
                }

                fieldsToShow.forEach { (key, value) ->
                    Text(text = "$key: $value", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
@Composable
fun CategoryCard(
    name: String,
    itemCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    items: List<ProductModel>,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- CATEGORY HEADER ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // --- CHANGE THIS ICON LOGIC ---
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Default.KeyboardArrowDown  // Points DOWN when open
                    else
                        Icons.Default.KeyboardArrowRight, // Points RIGHT when closed
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(text = "$itemCount", color = Color.White, modifier = Modifier.padding(4.dp))
                }
            }

            // --- PRODUCT CARDS (Shown when expanded) ---
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                items.forEach { product ->
                    // Re-implementing your original Product Card style here
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { navController.navigate("update_product/${product.id}") },
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product Image
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(60.dp)
                            ) {
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = product.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = product.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                // You can add a small detail here like the number of custom fields
                                Text(
                                    text = "Tap to view details",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}