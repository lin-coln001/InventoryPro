package com.management.inventorypro.ui.theme.screens.add

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.data.ProductViewModel
import com.management.inventorypro.models.ProductModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel()
) {
    // --- 1. STATE VARIABLES ---
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Uncategorized") }
    val context = LocalContext.current
    // 1. Get the live list of products from the ViewModel
    val allProducts = viewModel.products

    // 2. Extract unique categories from those products
    val existingCategories = remember(allProducts) {
        allProducts.map { it.category }
            .distinct()
            .filter { it.isNotBlank() }
            .sorted()
            .ifEmpty { listOf("Uncategorized", "Cars", "Electronics") }
    }




    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.selectedImageUri = uri
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(text = "New Inventory Item", style = MaterialTheme.typography.headlineMedium)
            }

            // --- 2. IMAGE UPLOAD ---
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("Add Photo", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // --- 3. PRODUCT NAME ---
            item {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // --- 4. CATEGORY SELECTOR ---
            item {
                CategorySelector(
                    currentCategory = category,
                    onCategorySelected = { category = it },
                    existingCategories = existingCategories
                )
            }

            // --- 5. CUSTOM DETAILS HEADER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Custom Details", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { viewModel.addNewField() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Field")
                    }
                }
            }

            // --- 6. DYNAMIC CUSTOM FIELDS ---
            itemsIndexed(viewModel.customFields) { index, field ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = field.key,
                        onValueChange = { viewModel.customFields[index] = field.copy(key = it) },
                        label = { Text("Label") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = field.value,
                        onValueChange = { viewModel.customFields[index] = field.copy(value = it) },
                        label = { Text("Value") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = { viewModel.removeField(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                    }
                }
            }

            // --- 7. SAVE BUTTON ---
            item {
                Button(
                    onClick = {
                        if (productName.isNotBlank()) {
                            val id = System.currentTimeMillis().toString()

                            // Convert list of fields to a Map for Firebase
                            val fieldsMap = viewModel.customFields.associate { it.key to it.value }

                            val newProduct = ProductModel(
                                id = id,
                                name = productName,
                                imageUrl = viewModel.selectedImageUri?.toString() ?: "",
                                category = category.trim().ifBlank { "Uncategorized" },
                                customFields = fieldsMap
                            )

                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            val database = FirebaseDatabase.getInstance().getReference("users")
                                .child(userId).child("inventory").child(id)

                            database.setValue(newProduct).addOnSuccessListener {
                                Toast.makeText(context, "Item added successfully!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please enter a product name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("Add Product")
                }
            }
        }

        if (viewModel.isUploading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

// --- HELPER COMPONENT (Outside the main screen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    currentCategory: String,
    onCategorySelected: (String) -> Unit,
    existingCategories: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentCategory,
                onValueChange = { onCategorySelected(it) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                placeholder = { Text("Select or type category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                existingCategories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
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