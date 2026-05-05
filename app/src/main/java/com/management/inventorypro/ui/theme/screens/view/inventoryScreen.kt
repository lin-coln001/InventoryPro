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
import com.management.inventorypro.ui.theme.DangerRed
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.ui.theme.screens.view.CategoryHeader
import com.management.inventorypro.util.ConnectivityObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewInventoryScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // --- REACTIVE ENGINE ---
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isSystemOnline by connectivityObserver.isOnline.collectAsState(initial = true)

    val themeColor = if (isSystemOnline) NeonCyan else DangerRed

    // --- OFFLINE SHIFT LOGIC ---

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

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("System Inventory", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        if (!isSystemOnline) {
                            Text("OFFLINE MODE - READ ONLY", fontSize = 10.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = themeColor
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(DeepMidnight), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColor)
            }
        } else if (productList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().background(DeepMidnight), contentAlignment = Alignment.Center) {
                Text("Database empty.", color = SoftCyan.copy(0.5f))
            }
        } else {
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
                            onToggle = { expandedCategory = if (expandedCategory == parentName) null else parentName },
                            themeColor = themeColor // Pass the shift
                        )
                    }

                    if (expandedCategory == parentName) {
                        val subGroups = allItemsInParent.filter { it.category.contains(" > ") }
                            .groupBy { it.category.substringAfter(" > ") }
                        val looseItems = allItemsInParent.filter { !it.category.contains(" > ") }

                        subGroups.forEach { (subName, items) ->
                            item(key = "sub_${parentName}_$subName") {
                                var subExpanded by remember { mutableStateOf(false) }
                                Column {
                                    Box(modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
                                        CategoryHeader(
                                            name = subName,
                                            itemCount = items.size,
                                            isExpanded = subExpanded,
                                            onToggle = { subExpanded = !subExpanded },
                                            themeColor = themeColor // Pass the shift
                                        )
                                    }
                                    if (subExpanded) {
                                        items.forEach { product ->
                                            Box(modifier = Modifier.padding(start = 32.dp, bottom = 4.dp)) {
                                                ProductRowItem(product, maxFields, themeColor, onClick = {
                                                    navController.navigate("update_product/${product.id}")
                                                })
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        items(looseItems, key = { "${it.id}_loose" }) { product ->
                            Box(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
                                ProductRowItem(product, maxFields, themeColor, onClick = {
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
    onToggle: () -> Unit,
    themeColor: Color // Added for shift
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BorderStroke(1.dp, if (isExpanded) themeColor.copy(0.4f) else Color.White.copy(0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isExpanded) themeColor else SoftCyan.copy(0.6f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = if (isExpanded) themeColor else Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            Surface(
                color = if (isExpanded) themeColor else SurfaceNavy,
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
fun ProductRowItem(product: ProductModel, maxFields: Int, themeColor: Color, onClick: () -> Unit) {
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

                val displayFields = if (maxFields <= 0) product.customFields.toList() else product.customFields.toList().take(maxFields)

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
                        color = themeColor.copy(0.8f), // Shifted
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = themeColor.copy(0.2f), // Shifted
                modifier = Modifier.size(18.dp)
            )
        }
    }
}