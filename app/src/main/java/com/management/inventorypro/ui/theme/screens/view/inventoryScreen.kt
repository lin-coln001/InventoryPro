package com.management.inventorypro.ui.theme.screens.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlin.collections.take
// This is the specific Coil import for AsyncImage
import coil.compose.AsyncImage
import com.management.inventorypro.data.InventoryViewModel
// Ensure this matches your actual package path for the Product model
import com.management.inventorypro.models.ProductModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewInventoryScreen(navController: NavController, viewModel: InventoryViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Current Items in stock.") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                // Explicitly tell the compiler we are using your Product class
                // Inside ViewInventoryScreen's LazyColumn
                items(viewModel.products) { product ->
                    ProductItemCard(
                        product = product,
                        maxFields = viewModel.maxFieldsToShow, // Pass the value from the ViewModel
                        onDelete = { viewModel.deleteProduct(product.id) },
                        onEdit = { navController.navigate("update_product/${product.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: ProductModel,
    maxFields: Int,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }, // Navigate to update screen
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image from Cloudinary
            Card(
                modifier = Modifier.size(70.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Inside ProductItemCard
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                val allFields = product.customFields.toList()

                val fieldsToDisplay = if (maxFields == 0) allFields else allFields.take(maxFields)

                fieldsToDisplay.forEach { (key, value) ->
                    Text(text = "$key: $value")
                }
            }
            }

            // DELETE BUTTON
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
