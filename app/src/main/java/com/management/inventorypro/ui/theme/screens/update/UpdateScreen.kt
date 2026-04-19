package com.management.inventorypro.ui.theme.screens.update

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.management.inventorypro.data.ProductViewModel
// IMPORTANT: Ensure this import matches your CustomField location
import com.management.inventorypro.models.CustomField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    navController: NavController,
    productId: String?,
    viewModel: ProductViewModel = viewModel()
) {
    // We use a local state for the name to keep the UI snappy
    var productName by remember { mutableStateOf("") }

    // 1. Fetch data when the screen loads
    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.fetchProductById(productId) { product ->
                productName = product.name

                // Clear the ViewModel's list and repopulate with saved data
                viewModel.customFields.clear()
                product.customFields.forEach { (k, v) ->
                    viewModel.customFields.add(CustomField(k, v))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product Name Field
            item {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Custom Fields Section Header
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

            // 2. Dynamic List of Custom Fields
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

            // 3. Save Changes Button
            item {
                Button(
                    onClick = {
                        if (productId != null) {
                            viewModel.updateProduct(productId, productName) {
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = productName.isNotBlank() && !viewModel.isUploading
                ) {
                    if (viewModel.isUploading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}