package com.management.inventorypro.ui.theme.screens.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.data.ProductViewModel
import com.management.inventorypro.ui.theme.DangerRed
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.ui.theme.screens.add.CategorySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel()
) {
    var productName by remember { mutableStateOf("") }

    // --- TWO-LEVEL HIERARCHY STATE ---
    var mainCategory by remember { mutableStateOf("Uncategorized") }
    var subCategory by remember { mutableStateOf("") }

    val allProducts by viewModel.products.collectAsState()

    // Top-level categories (Main)
    val dynamicMainCategories = remember(allProducts) {
        allProducts.map { it.category.split(" > ").first() }
            .filter { it.isNotBlank() }
            .distinct().sorted()
            .ifEmpty { listOf("Uncategorized") }
    }

    // Contextual Sub-categories based on selected Main Category
    val dynamicSubCategories = remember(mainCategory, allProducts) {
        allProducts
            .filter { it.category.startsWith("$mainCategory > ") }
            .map { it.category.substringAfter(" > ") }
            .distinct().sorted()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.selectedImageUri = uri }

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = { Text("Add New Entry", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = NeonCyan
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- IMAGE SELECTION SECTION ---
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceNavy)
                                .border(BorderStroke(1.dp, NeonCyan.copy(0.2f)), RoundedCornerShape(20.dp))
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (viewModel.selectedImageUri != null) {
                                AsyncImage(
                                    model = viewModel.selectedImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                                    Text("Upload Image", color = SoftCyan, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // --- PRODUCT NAME INPUT ---
                item {
                    CyberTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = "Product Name",
                        icon = Icons.Default.List
                    )
                }

                // --- CLASSIFICATION SECTION (TWO LEVELS) ---
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Classification", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        // Main Category Selector
                        CategorySelector(
                            label = "Main Category",
                            currentCategory = mainCategory,
                            onCategorySelected = {
                                mainCategory = it
                                subCategory = "" // Reset sub-category when main changes
                            },
                            existingCategories = dynamicMainCategories
                        )

                        // Sub-Category Selector
                        CategorySelector(
                            label = "Sub-Category (Optional)",
                            currentCategory = subCategory,
                            onCategorySelected = { subCategory = it },
                            existingCategories = dynamicSubCategories
                        )
                    }
                }

                // --- DYNAMIC CUSTOM FIELDS SECTION ---
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Custom Metadata", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        TextButton(onClick = { viewModel.addNewField() }, colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("New Field")
                        }
                    }
                }

                itemsIndexed(viewModel.customFields) { index, field ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CyberTextField(
                            value = field.key,
                            onValueChange = { viewModel.customFields[index] = field.copy(key = it) },
                            label = "Key",
                            modifier = Modifier.weight(1f)
                        )
                        CyberTextField(
                            value = field.value,
                            onValueChange = { viewModel.customFields[index] = field.copy(value = it) },
                            label = "Value",
                            modifier = Modifier.weight(1.5f)
                        )
                        IconButton(onClick = { viewModel.removeField(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DangerRed.copy(0.7f))
                        }
                    }
                }

                // --- ACTION BUTTON ---
                item {
                    Button(
                        onClick = {
                            if (productName.isNotBlank()) {
                                // Logic: Join Main and Sub if Sub exists
                                val finalPath = if (subCategory.isNotBlank()) "$mainCategory > $subCategory" else mainCategory

                                val currentUri = viewModel.selectedImageUri
                                val newProductId = FirebaseDatabase.getInstance().getReference("users").push().key
                                    ?: System.currentTimeMillis().toString()

                                if (currentUri != null) {
                                    viewModel.uploadToCloudinary(currentUri) { webUrl ->
                                        viewModel.saveProductToFirebase(
                                            productId = newProductId,
                                            name = productName,
                                            category = finalPath,
                                            imageUrl = webUrl,
                                            onComplete = { navController.popBackStack() }
                                        )
                                    }
                                } else {
                                    viewModel.saveProductToFirebase(
                                        productId = newProductId,
                                        name = productName,
                                        category = finalPath,
                                        imageUrl = "",
                                        onComplete = { navController.popBackStack() }
                                    )
                                }
                            }
                        },
                        enabled = !viewModel.isUploading,
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepMidnight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (viewModel.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = DeepMidnight, strokeWidth = 3.dp)
                        } else {
                            Text("ADD ITEM", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SoftCyan.copy(0.5f)) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = NeonCyan.copy(0.6f)) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color.White.copy(0.1f),
            focusedContainerColor = SurfaceNavy,
            unfocusedContainerColor = SurfaceNavy,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = NeonCyan
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    label: String,
    currentCategory: String,
    onCategorySelected: (String) -> Unit,
    existingCategories: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = SoftCyan.copy(0.6f), style = MaterialTheme.typography.labelLarge)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = currentCategory,
                onValueChange = { onCategorySelected(it) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.White.copy(0.1f),
                    focusedContainerColor = SurfaceNavy,
                    unfocusedContainerColor = SurfaceNavy,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceNavy).border(1.dp, NeonCyan.copy(0.2f), RoundedCornerShape(8.dp))
            ) {
                existingCategories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption, color = Color.White) },
                        onClick = {
                            onCategorySelected(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}