package com.management.inventorypro.ui.theme.screens.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.management.inventorypro.models.ProductModel
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.ui.theme.screens.view.CategoryHeader
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewInventoryScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val inventoryRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("inventory")
    val settingsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("settings")

    val productList = remember { mutableStateListOf<ProductModel>() }
    var isLoading by remember { mutableStateOf(true) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var maxFields by remember { mutableIntStateOf(2) }

    LaunchedEffect(userId) {
        // Fetch Max Fields Settings
        settingsRef.child("maxVisibleFields").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                maxFields = snapshot.getValue(Int::class.java) ?: 0
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch Inventory Data
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

    // Grouping by the full hierarchy path (e.g., "Electronics > Phones")
    val groupedItems = productList.groupBy { it.category.ifEmpty { "Uncategorized" } }
    // Helper class to represent the hierarchy in memory
    data class CategoryNode(
        val name: String,
        val subCategories: Map<String, List<ProductModel>>,
        val looseItems: List<ProductModel>
    )

// Process the products into a hierarchical structure
    val hierarchicalData = remember(productList) {
        productList.groupBy { it.category.split(" > ").first().ifEmpty { "Uncategorized" } }
            .mapValues { (parentName, productsInParent) ->
                // Separate items that belong to a subcategory from those that are loose
                val subGroups = productsInParent.filter { it.category.contains(" > ") }
                    .groupBy { it.category.substringAfter(" > ") }

                val loose = productsInParent.filter { !it.category.contains(" > ") }

                CategoryNode(parentName, subGroups, loose)
            }
    }

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = { Text("System Inventory", fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = NeonCyan
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(DeepMidnight), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else if (productList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(DeepMidnight), contentAlignment = Alignment.Center) {
                Text("Database empty.", color = SoftCyan.copy(0.5f))
            }
        } else {
            // Inside ViewInventoryScreen.kt
            val masterGroups = productList.groupBy { it.category.split(" > ").first().ifEmpty { "Uncategorized" } }

            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                masterGroups.forEach { (parentName, allItemsInParent) ->
                    item(key = "parent_$parentName") {
                        CategoryHeader(
                            name = parentName,
                            itemCount = allItemsInParent.size,
                            isExpanded = expandedCategory == parentName,
                            onToggle = { expandedCategory = if (expandedCategory == parentName) null else parentName }
                        )
                    }

                    if (expandedCategory == parentName) {
                        val subGroups = allItemsInParent.filter { it.category.contains(" > ") }
                            .groupBy { it.category.substringAfter(" > ") }
                        val looseItems = allItemsInParent.filter { !it.category.contains(" > ") }

                        // A. SUB-CATEGORY CARDS
                        subGroups.forEach { (subName, items) ->
                            item(key = "sub_${parentName}_$subName") {
                                var subExpanded by remember { mutableStateOf(false) }
                                Column {
                                    Box(modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
                                        CategoryHeader(
                                            name = subName,
                                            itemCount = items.size,
                                            isExpanded = subExpanded,
                                            onToggle = { subExpanded = !subExpanded }
                                        )
                                    }
                                    if (subExpanded) {
                                        items.forEach { product ->
                                            Box(modifier = Modifier.padding(start = 32.dp, bottom = 4.dp)) {
                                                ProductRowItem(product, maxFields, onClick = {
                                                    navController.navigate("update_product/${product.id}")
                                                })
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // B. LOOSE ITEMS IN THIS CATEGORY
                        items(looseItems, key = { "${it.id}_loose" }) { product ->
                            Box(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
                                ProductRowItem(product, maxFields, onClick = {
                                    navController.navigate("update_product/${product.id}")
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    name: String,
    itemCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BorderStroke(1.dp, if (isExpanded) NeonCyan.copy(0.4f) else Color.White.copy(0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isExpanded) NeonCyan else SoftCyan.copy(0.6f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = if (isExpanded) NeonCyan else Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            Surface(
                color = if (isExpanded) NeonCyan else SurfaceNavy,
                shape = RoundedCornerShape(8.dp),
                border = if (!isExpanded) BorderStroke(1.dp, SoftCyan.copy(0.3f)) else null
            ) {
                Text(
                    text = "$itemCount",
                    color = if (isExpanded) DeepMidnight else SoftCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductRowItem(product: ProductModel, maxFields: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepMidnight.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(300)
                    .dispatcher(Dispatchers.IO)
                    .size(Size(160, 160)) // Critical for low-RAM performance
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceNavy),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                // Dynamic Metadata Display
                val displayFields = if (maxFields <= 0) {
                    product.customFields.toList()
                } else {
                    product.customFields.toList().take(maxFields)
                }

                displayFields.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        fontSize = 12.sp,
                        color = SoftCyan.copy(0.6f)
                    )
                }

                if (maxFields > 0 && product.customFields.size > maxFields) {
                    Text(
                        text = "+${product.customFields.size - maxFields} more",
                        fontSize = 11.sp,
                        color = NeonCyan.copy(0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = SoftCyan.copy(0.2f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
@Composable
fun SubCategoryFolderHeader(name: String, count: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 12.dp),
        color = SurfaceNavy.copy(alpha = 0.4f),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.List,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = name.uppercase(),
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count",
                color = DeepMidnight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(SoftCyan.copy(0.6f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}