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
import androidx.compose.foundation.shape.CircleShape
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
import com.management.inventorypro.ui.theme.DangerRed
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.ui.theme.screens.add.CategorySelector
import com.management.inventorypro.ui.theme.screens.update.UpdateCyberTextField


// Reusing your defined palette
//val DeepMidnight = Color(0xFF0A0E1A)
//val SurfaceNavy = Color(0xFF161C2C)
//val NeonCyan = Color(0xFF00E5FF)
//val SoftCyan = Color(0xFFB2EBF2)
//val DangerRed = Color(0xFFFF5252)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    navController: NavController,
    productId: String?,
    viewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val allProducts by viewModel.products.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val database = FirebaseDatabase.getInstance().getReference("users")
        .child(userId).child("inventory").child(productId ?: "")

    // --- TWO-LEVEL STATE ---
    var productName by remember { mutableStateOf("") }
    var mainCategory by remember { mutableStateOf("Uncategorized") }
    var subCategory by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Dynamic categories for the main dropdown
    val dynamicMainCategories = remember(allProducts) {
        allProducts.map { it.category.split(" > ").first() }
            .filter { it.isNotBlank() }
            .distinct().sorted()
            .ifEmpty { listOf("Uncategorized") }
    }

    // Contextual Sub-categories based on selection
    val dynamicSubCategories = remember(mainCategory, allProducts) {
        allProducts
            .filter { it.category.startsWith("$mainCategory > ") }
            .map { it.category.substringAfter(" > ") }
            .distinct().sorted()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.selectedImageUri = uri }

    LaunchedEffect(Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val product = snapshot.getValue(ProductModel::class.java)
            product?.let {
                productName = it.name
                imageUrl = it.imageUrl

                // Split path into Main and Sub
                if (it.category.contains(" > ")) {
                    mainCategory = it.category.substringBefore(" > ")
                    subCategory = it.category.substringAfter(" > ")
                } else {
                    mainCategory = it.category.ifEmpty { "Uncategorized" }
                    subCategory = ""
                }

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
                title = { Text("Update Item", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = NeonCyan
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = SurfaceNavy,
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
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isUploading
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Delete")
                    }

                    Button(
                        onClick = {
                            if (productName.isNotBlank()) {
                                // RE-JOIN path for saving
                                val finalPath = if (subCategory.isNotBlank()) "$mainCategory > $subCategory" else mainCategory
                                val currentUri = viewModel.selectedImageUri

                                val saveAction = { finalImageUrl: String ->
                                    viewModel.saveProductToFirebase(
                                        productId = productId ?: "",
                                        name = productName,
                                        category = finalPath,
                                        imageUrl = finalImageUrl,
                                        onComplete = {
                                            Toast.makeText(context, "Entry Updated", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                if (currentUri != null && currentUri.toString().startsWith("content://")) {
                                    viewModel.uploadToCloudinary(currentUri) { webUrl ->
                                        saveAction(webUrl)
                                    }
                                } else {
                                    saveAction(imageUrl)
                                }
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepMidnight),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isUploading
                    ) {
                        if (viewModel.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepMidnight)
                        } else {
                            Icon(Icons.Default.Done, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // IMAGE SECTION
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
                            AsyncImage(
                                model = displayUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                        }
                    }
                }
            }

            item {
                UpdateCyberTextField(value = productName, onValueChange = { productName = it }, label = "Item Name")
            }

            // CLASSIFICATION SECTION
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Classification", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    com.management.inventorypro.ui.theme.screens.add.CategorySelector(
                        label = "Main Category",
                        currentCategory = mainCategory,
                        onCategorySelected = {
                            mainCategory = it
                            subCategory = ""
                        },
                        existingCategories = dynamicMainCategories
                    )

                    com.management.inventorypro.ui.theme.screens.add.CategorySelector(
                        label = "Sub-Category (Optional)",
                        currentCategory = subCategory,
                        onCategorySelected = { subCategory = it },
                        existingCategories = dynamicSubCategories
                    )
                }
            }

            // METADATA SECTION
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
                title = { Text("Delete Item") },
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