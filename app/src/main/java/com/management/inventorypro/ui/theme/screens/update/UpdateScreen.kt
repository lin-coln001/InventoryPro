package com.management.inventorypro.ui.theme.screens.update

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
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
import com.management.inventorypro.models.CustomField
import com.management.inventorypro.models.ProductModel
import com.management.inventorypro.ui.theme.screens.add.CategorySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    navController: NavController,
    productId: String?,
    viewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val database = FirebaseDatabase.getInstance().getReference("users")
        .child(userId).child("inventory").child(productId ?: "")

    // --- State Variables ---
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Uncategorized") }
    var imageUrl by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.selectedImageUri = uri
    }

    // --- LOAD EXISTING DATA ---
    LaunchedEffect(Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val product = snapshot.getValue(ProductModel::class.java)
            product?.let {
                productName = it.name
                category = it.category
                imageUrl = it.imageUrl

                viewModel.customFields.clear()
                it.customFields.forEach { (k, v) ->
                    viewModel.customFields.add(CustomField(k, v))
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load product", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Product") }) },
        // --- FIXED BOTTOM NAVIGATION BAR ---
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete Button (Smaller, Outlined)
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }

                    // Save Button (Primary, Fills more space)
                    Button(
                        onClick = {
                            if (productName.isNotBlank()) {
                                val fieldsMap = viewModel.customFields.associate { it.key to it.value }
                                val finalImageUrl = viewModel.selectedImageUri?.toString() ?: imageUrl

                                val updatedProduct = ProductModel(
                                    id = productId ?: "",
                                    name = productName,
                                    imageUrl = finalImageUrl,
                                    category = category.trim().ifBlank { "Uncategorized" },
                                    customFields = fieldsMap
                                )

                                database.setValue(updatedProduct).addOnSuccessListener {
                                    Toast.makeText(context, "Updated successfully!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            } else {
                                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes")
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Image Section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            val displayUri = viewModel.selectedImageUri ?: if (imageUrl.isNotEmpty()) Uri.parse(imageUrl) else null
                            if (displayUri != null) {
                                AsyncImage(
                                    model = displayUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text("Change Photo", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // 2. Name Input
                item {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 3. Category Selector
                item {
                    CategorySelector(
                        currentCategory = category,
                        onCategorySelected = { category = it },
                        existingCategories = listOf("Uncategorized", "Cars", "Electronics", "Furniture")
                    )
                }

                // 4. Custom Fields Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Custom Details", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { viewModel.addNewField() }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add Field")
                        }
                    }
                }

                // 5. Dynamic List (Custom Fields)
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
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = field.value,
                            onValueChange = { viewModel.customFields[index] = field.copy(value = it) },
                            label = { Text("Value") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeField(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                        }
                    }
                }

                // Extra spacer so the last field isn't hidden behind the BottomAppBar
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }

            // --- DELETE CONFIRMATION DIALOG ---
            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Delete Product") },
                    text = { Text("Are you sure you want to permanently delete '$productName'? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                database.removeValue().addOnSuccessListener {
                                    Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}