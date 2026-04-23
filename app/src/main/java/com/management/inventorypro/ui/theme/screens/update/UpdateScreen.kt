package com.management.inventorypro.ui.theme.screens.update

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Done
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.data.ProductViewModel
import com.management.inventorypro.models.CustomField
import com.management.inventorypro.models.ProductModel
import com.management.inventorypro.ui.theme.screens.add.CategorySelector

// Reusing your defined palette
val DeepMidnight = Color(0xFF0A0E1A)
val SurfaceNavy = Color(0xFF161C2C)
val NeonCyan = Color(0xFF00E5FF)
val SoftCyan = Color(0xFFB2EBF2)
val DangerRed = Color(0xFFFF5252)

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

    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Uncategorized") }
    var imageUrl by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.selectedImageUri = uri }

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
        }
    }

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = { Text("Update Entry", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = NeonCyan
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = SurfaceNavy,
                tonalElevation = 0.dp,
                modifier = Modifier.height(80.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }

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
                                    Toast.makeText(context, "System Updated", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepMidnight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Image Section
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
                        val displayUri = viewModel.selectedImageUri ?: if (imageUrl.isNotEmpty()) Uri.parse(imageUrl) else null
                        if (displayUri != null) {
                            AsyncImage(model = displayUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                        }
                    }
                }
            }

            item {
                UpdateCyberTextField(value = productName, onValueChange = { productName = it }, label = "Product Name")
            }

            item {
                // Assuming CategorySelector was already styled in the previous step
                CategorySelector(
                    currentCategory = category,
                    onCategorySelected = { category = it },
                    existingCategories = listOf("Uncategorized", "Cars", "Electronics", "Furniture")
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Advanced Metadata", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = { viewModel.addNewField() }, colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Add Field")
                    }
                }
            }

            itemsIndexed(viewModel.customFields) { index, field ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    UpdateCyberTextField(
                        value = field.key,
                        onValueChange = { viewModel.customFields[index] = field.copy(key = it) },
                        label = "Label",
                        modifier = Modifier.weight(1f)
                    )
                    UpdateCyberTextField(
                        value = field.value,
                        onValueChange = { viewModel.customFields[index] = field.copy(value = it) },
                        label = "Value",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.removeField(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DangerRed.copy(0.6f))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                containerColor = SurfaceNavy,
                titleContentColor = NeonCyan,
                textContentColor = Color.White,
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Entry") },
                text = { Text("Permanently remove '$productName' from the database?") },
                confirmButton = {
                    TextButton(onClick = {
                        database.removeValue().addOnSuccessListener {
                            navController.popBackStack()
                        }
                    }) { Text("Confirm", color = DangerRed) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel", color = SoftCyan) }
                }
            )
        }
    }
}

@Composable
fun UpdateCyberTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SoftCyan.copy(0.5f)) },
        modifier = modifier.fillMaxWidth(),
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