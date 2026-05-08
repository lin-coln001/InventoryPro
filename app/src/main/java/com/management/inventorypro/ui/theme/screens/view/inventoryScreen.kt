package com.management.inventorypro.ui.theme.screens.view

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.management.inventorypro.models.ProductModel
import com.management.inventorypro.ui.theme.DangerRed
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.util.ConnectivityObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewInventoryScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isSystemOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val themeColor = if (isSystemOnline) NeonCyan else DangerRed

    val inventoryRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("inventory")
    val settingsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("settings")

    val productList = remember { mutableStateListOf<ProductModel>() }
    var isLoading by remember { mutableStateOf(true) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var maxFields by remember { mutableIntStateOf(2) }

    var searchQuery by remember { mutableStateOf("") }
    var currentSortOption by remember { mutableStateOf("Newest") }
    val sortOptions = listOf("Newest", "A-Z")

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

    // --- ADVANCED SEARCH ENGINE (Includes Custom Fields) ---
// 1. First, we filter the raw products down to ONLY what matches the search
    val filteredAndSortedList = productList.filter { product ->
        val nameMatch = product.name.contains(searchQuery, ignoreCase = true)
        val categoryMatch = product.category.contains(searchQuery, ignoreCase = true)
        val customFieldsMatch = product.customFields.values.any { it.contains(searchQuery, ignoreCase = true) }

        nameMatch || categoryMatch || customFieldsMatch
    }.let { list ->
        if (currentSortOption == "A-Z") list.sortedBy { it.name.lowercase() }
        else list.asReversed()
    }

// 2. IMPORTANT: Group the list AFTER it has been filtered.
// This ensures that if an item is "far apart" from another,
// only those two items (and their specific headers) show up.
    val masterGroups = filteredAndSortedList.groupBy {
        it.category.split(" > ").first().ifEmpty { "Uncategorized" }
    }
    // Auto-expand categories when the user types more than 2 characters
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            // You can set a specific category to expand or, if you want all open,
            // you would need to change expandedCategory to a list.
            // For now, let's at least clear the "null" state if results are found.
            if (masterGroups.isNotEmpty()) {
                expandedCategory = masterGroups.keys.firstOrNull()
            }
        }
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search items or categories...", color = SoftCyan.copy(0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColor) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SoftCyan)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColor,
                    unfocusedBorderColor = SoftCyan.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = SurfaceNavy.copy(alpha = 0.3f),
                    unfocusedContainerColor = SurfaceNavy.copy(alpha = 0.3f),
                    cursorColor = themeColor,
                    focusedLeadingIconColor = themeColor,
                    unfocusedLeadingIconColor = SoftCyan.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortOptions) { option ->
                    FilterChip(
                        selected = currentSortOption == option,
                        onClick = { currentSortOption = option },
                        label = { Text(option, fontSize = 12.sp) },
                        leadingIcon = {
                            if (currentSortOption == option) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColor,
                            selectedLabelColor = DeepMidnight,
                            containerColor = SurfaceNavy,
                            labelColor = SoftCyan
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (currentSortOption == option) themeColor else Color.Transparent,
                            enabled = true,
                            selected = currentSortOption == option
                        )
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = themeColor)
                }
            } else if (filteredAndSortedList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isEmpty()) "Database empty." else "No matches found.",
                        color = SoftCyan.copy(0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    masterGroups.forEach { (parentName, allItemsInParent) ->
                        item(key = "parent_$parentName") {
                            CategoryHeader(
                                name = parentName,
                                itemCount = allItemsInParent.size,
                                isExpanded = expandedCategory == parentName,
                                onToggle = { expandedCategory = if (expandedCategory == parentName) null else parentName },
                                themeColor = themeColor
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
                                                themeColor = themeColor
                                            )
                                        }
                                        if (subExpanded) {
                                            items.forEach { product ->
                                                Box(modifier = Modifier.padding(start = 32.dp, bottom = 4.dp)) {
                                                    ProductRowItem(
                                                        product = product,
                                                        maxFields = maxFields,
                                                        themeColor = themeColor,
                                                        searchQuery = searchQuery, // FIXED
                                                        onClick = {
                                                            navController.navigate("update_product/${product.id}")
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            items(looseItems, key = { "${it.id}_loose" }) { product ->
                                Box(modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)) {
                                    ProductRowItem(
                                        product = product,
                                        maxFields = maxFields,
                                        themeColor = themeColor,
                                        searchQuery = searchQuery, // FIXED
                                        onClick = {
                                            navController.navigate("update_product/${product.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
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
    themeColor: Color
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
fun ProductRowItem(
    product: ProductModel,
    maxFields: Int,
    themeColor: Color,
    searchQuery: String,
    onClick: () -> Unit
) {
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
                    text = getHighlightedText(
                        text = product.name,
                        query = searchQuery,
                        highlightColor = NeonCyan
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                val displayFields = if (maxFields <= 0) product.customFields.toList() else product.customFields.toList().take(maxFields)

                // Inside ProductRowItem, where it displays custom fields:
                displayFields.forEach { (key, value) ->
                    Text(
                        text = buildAnnotatedString {
                            append("$key: ")
                            append(getHighlightedText(value, searchQuery, NeonCyan))
                        },
                        fontSize = 12.sp,
                        color = SoftCyan.copy(0.6f)
                    )
                }
                if (maxFields > 0 && product.customFields.size > maxFields) {
                    Text(
                        text = "+${product.customFields.size - maxFields} more",
                        fontSize = 11.sp,
                        color = themeColor.copy(0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = themeColor.copy(0.2f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun getHighlightedText(text: String, query: String, highlightColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val lowercaseText = text.lowercase()
        val lowercaseQuery = query.lowercase()

        if (query.isEmpty() || !lowercaseText.contains(lowercaseQuery)) {
            append(text)
        } else {
            var start = 0
            while (start < text.length) {
                val index = lowercaseText.indexOf(lowercaseQuery, start)
                if (index == -1) {
                    append(text.substring(start))
                    break
                }
                append(text.substring(start, index))
                withStyle(style = SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
                    append(text.substring(index, index + query.length))
                }
                start = index + query.length
            }
        }
    }
}