package com.management.inventorypro.ui.theme.screens.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.management.inventorypro.models.ProductModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewInventoryScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Database References
    val inventoryRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("inventory")
    val settingsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("settings")

    // State Variables
    val productList = remember { mutableStateListOf<ProductModel>() }
    var isLoading by remember { mutableStateOf(true) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // The detail level setting (default to 2)
    var maxFields by remember { mutableIntStateOf(2) }

    // --- 1. FETCH SETTINGS & PRODUCTS ---
    LaunchedEffect(userId) {
        // Fetch Max Fields Setting
        settingsRef.child("maxVisibleFields").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                maxFields = snapshot.getValue(Int::class.java) ?: 0
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Inventory Items
        inventoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (productSnap in snapshot.children) {
                    val product = productSnap.getValue(ProductModel::class.java)
                    product?.let { productList.add(it) }
                }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

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
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                groupedItems.forEach { (categoryName, items) ->
                    item {
                        CategoryCard(
                            name = categoryName,
                            itemCount = items.size,
                            isExpanded = expandedCategory == categoryName,
                            onToggle = { expandedCategory = if (expandedCategory == categoryName) null else categoryName },
                            items = items,
                            maxFields = maxFields, // Pass setting to children
                            navController = navController
                        )
                    }
                }
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
    maxFields: Int,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
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

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                items.forEach { product ->
                    ProductItemDetailCard(
                        product = product,
                        maxFields = maxFields,
                        onClick = { navController.navigate("update_product/${product.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItemDetailCard(product: ProductModel, maxFields: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(50.dp)) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                // Logic: 0 = Show All, otherwise show the specific count
                val displayFields = if (maxFields == 0) {
                    product.customFields.toList()
                } else {
                    product.customFields.toList().take(maxFields)
                }

                displayFields.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }

                // Show a hint if there are hidden fields
                if (maxFields != 0 && product.customFields.size > maxFields) {
                    Text(
                        text = "+${product.customFields.size - maxFields} more details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}