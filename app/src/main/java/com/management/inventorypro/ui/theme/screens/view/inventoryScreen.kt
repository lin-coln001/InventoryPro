package com.management.inventorypro.ui.theme.screens.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import kotlinx.coroutines.Dispatchers

// Use the same colors as Dashboard
//val DeepMidnight = Color(0xFF0A0E1A)
//val SurfaceNavy = Color(0xFF161C2C)
//val NeonCyan = Color(0xFF00E5FF)
//val SoftCyan = Color(0xFFB2EBF2)

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
        settingsRef.child("maxVisibleFields").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                maxFields = snapshot.getValue(Int::class.java) ?: 0
            }
            override fun onCancelled(error: DatabaseError) {}
        })

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
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(DeepMidnight)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedItems.forEach { (categoryName, items) ->
                    // 1. HEADER ITEM
                    item(key = "header_$categoryName") {
                        CategoryHeader(
                            name = categoryName,
                            itemCount = items.size,
                            isExpanded = expandedCategory == categoryName,
                            onToggle = {
                                expandedCategory = if (expandedCategory == categoryName) null else categoryName
                            }
                        )
                    }

                    // 2. CHILD ITEMS (Only if expanded)
                    if (expandedCategory == categoryName) {
                        items(
                            items = items,
                            key = { product -> product.id }, // Ensures scroll performance
                            contentType = { "product" }
                        ) { product ->
                            ProductRowItem(
                                product = product,
                                maxFields = maxFields,
                                onClick = { navController.navigate("update_product/${product.id}") }
                            )
                        }
                    }
                }
                // Add extra padding at the bottom for scroll breathing room
                item { Spacer(modifier = Modifier.height(24.dp)) }
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
                fontSize = 18.sp,
                color = if (isExpanded) NeonCyan else Color.White,
                modifier = Modifier.weight(1f)
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
                    .crossfade(300) // Shorter crossfade is easier on old GPUs
                    .dispatcher(Dispatchers.IO) // Ensure image decoding is off-thread
                    .placeholder(null) // Using no placeholder is sometimes faster on low-RAM
                    .size(Size(160, 160))
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
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BorderStroke(1.dp, if (isExpanded) NeonCyan.copy(0.4f) else Color.White.copy(0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (isExpanded) NeonCyan else SoftCyan.copy(0.6f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = if (isExpanded) NeonCyan else Color.White,
                    modifier = Modifier.weight(1f)
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

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductItemDetailCard(product: ProductModel, maxFields: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DeepMidnight.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceNavy)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .size(200, 200) // Downsample to 200px—much lighter on RAM
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                val displayFields = if (maxFields == 0) {
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

                if (maxFields != 0 && product.customFields.size > maxFields) {
                    Text(
                        text = "+${product.customFields.size - maxFields} details",
                        fontSize = 11.sp,
                        color = NeonCyan.copy(0.8f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
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