package com.management.inventorypro.ui.theme.screens.add

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.data.ProductViewModel
import com.management.inventorypro.models.ProductModel

// --- SHARED COLORS ---
val DeepMidnight = Color(0xFF0A0E1A)
val SurfaceNavy = Color(0xFF161C2C)
val NeonCyan = Color(0xFF00E5FF)
val SoftCyan = Color(0xFFB2EBF2)
val DangerRed = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel()
) {
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Uncategorized") }
    val context = LocalContext.current
    val allProducts by viewModel.products.collectAsState()

    val dynamicCategories = remember(allProducts) {
        allProducts.map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .ifEmpty { listOf("Uncategorized") }
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
                            if (viewModel.selectedImageUri != null) {
                                AsyncImage(model = viewModel.selectedImageUri, contentDescription = null,
                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = NeonCyan)
                                    Text("Upload Image", color = SoftCyan, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Product Name
                item {
                    CyberTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = "Product Name",
                        icon = Icons.Default.List
                    )
                }

                // Category Selector
                item {
                    CategorySelector(
                        currentCategory = category,
                        onCategorySelected = { category = it },
                        existingCategories = dynamicCategories
                    )
                }

                // Custom Details Header
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

                // Dynamic Custom Fields
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

                // Save Button
                item {
                    Button(
                        onClick = {
                            if (productName.isNotBlank()) {
                                val id = System.currentTimeMillis().toString()
                                val fieldsMap = viewModel.customFields.associate { it.key to it.value }
                                val newProduct = ProductModel(
                                    id = id, name = productName,
                                    imageUrl = viewModel.selectedImageUri?.toString() ?: "",
                                    category = category.trim().ifBlank { "Uncategorized" },
                                    customFields = fieldsMap
                                )
                                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                FirebaseDatabase.getInstance().getReference("users")
                                    .child(userId).child("inventory").child(id)
                                    .setValue(newProduct).addOnSuccessListener {
                                        Toast.makeText(context, "Entry Saved", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepMidnight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("INITIATE SAVE", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
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
    currentCategory: String,
    onCategorySelected: (String) -> Unit,
    existingCategories: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Classification", color = SoftCyan.copy(0.6f), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))

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