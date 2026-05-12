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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.data.ProductViewModel
import com.management.inventorypro.ui.theme.*
import com.management.inventorypro.util.ConnectivityObserver

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // PRECISION ERROR HANDLING
    val nameRequester = remember { BringIntoViewRequester() }
    val connectivityObserver = remember { ConnectivityObserver(context) }

    val isSystemOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    var firstErrorIndex by remember { mutableStateOf<Int?>(null) }
    var nameHasError by remember { mutableStateOf(false) }

    val themeColor = if (isSystemOnline) NeonCyan else DangerRed

    var productName by remember { mutableStateOf("") }
    var mainCategory by remember { mutableStateOf("Uncategorized") }
    var subCategory by remember { mutableStateOf("") }
    val allProducts by viewModel.products.collectAsState()

    val dynamicMainCategories = remember(allProducts) {
        allProducts.map { it.category.split(" > ").first() }
            .filter { it.isNotBlank() }
            .distinct().sorted()
            .ifEmpty { listOf("Uncategorized") }
    }

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
                title = {
                    Column {
                        Text("Add New Entry", fontWeight = FontWeight.Bold)
                        if (!isSystemOnline) {
                            Text("OFFLINE MODE - WRITE LOCKED", fontSize = 10.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = themeColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = themeColor
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Index 0: Image Upload
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceNavy)
                                .border(BorderStroke(1.dp, themeColor.copy(0.2f)), RoundedCornerShape(20.dp))
                                .clickable { if (isSystemOnline) galleryLauncher.launch("image/*") },
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
                                    Icon(Icons.Default.Add, contentDescription = null, tint = themeColor)
                                    Text("Upload Image", color = if (isSystemOnline) SoftCyan else DangerRed, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Index 1: Product Name with Precision Relocation
                item {
                    Box(modifier = Modifier.bringIntoViewRequester(nameRequester)) {
                        CyberTextField(
                            value = productName,
                            onValueChange = {
                                productName = it
                                if(it.isNotBlank()) nameHasError = false
                            },
                            label = "Item Name",
                            icon = Icons.Default.List,
                            themeColor = if (nameHasError) DangerRed else themeColor
                        )
                    }
                }

                // Index 2: Classification
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Categorisation", color = themeColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        CategorySelector("Main Category", mainCategory, { mainCategory = it; subCategory = "" }, dynamicMainCategories, themeColor)
                        CategorySelector("Sub-Category (Optional)", subCategory, { subCategory = it }, dynamicSubCategories, themeColor)
                    }
                }

                // Index 3: Custom Data Header
                item {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Custom data", color = themeColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            TextButton(onClick = { viewModel.addNewField() }) {
                                Icon(Icons.Default.Add, null, tint = themeColor)
                                Spacer(Modifier.width(4.dp))
                                Text("New Field", color = themeColor)
                            }
                        }
                        if (firstErrorIndex != null) {
                            Text(
                                "⚠️ ERROR: Empty fields detected at row ${firstErrorIndex!! + 1}",
                                color = DangerRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Index 4 onwards: Dynamic Custom Fields
                itemsIndexed(viewModel.customFields) { index, field ->
                    val isBroken = index == firstErrorIndex
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CyberTextField(
                            value = field.key,
                            onValueChange = {
                                viewModel.customFields[index] = field.copy(key = it)
                                if (it.isNotBlank()) firstErrorIndex = null
                            },
                            label = "Key",
                            modifier = Modifier.weight(1f),
                            themeColor = if (isBroken && field.key.isBlank()) DangerRed else themeColor
                        )
                        CyberTextField(
                            value = field.value,
                            onValueChange = {
                                viewModel.customFields[index] = field.copy(value = it)
                                if (it.isNotBlank()) firstErrorIndex = null
                            },
                            label = "Value",
                            modifier = Modifier.weight(1.5f),
                            themeColor = if (isBroken && field.value.isBlank()) DangerRed else themeColor
                        )
                        IconButton(onClick = { viewModel.removeField(index) }) {
                            Icon(Icons.Default.Delete, null, tint = DangerRed.copy(0.7f))
                        }
                    }
                }

                // Final Index: Submit Button
                item {
                    Button(
                        onClick = {
                            if (!isSystemOnline) {
                                Toast.makeText(context, "YOU ARE OFFLINE: go online to continue", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // FIX: Precise Scroll to Name Error
                            if (productName.trim().isBlank()) {
                                nameHasError = true
                                scope.launch {
                                    nameRequester.bringIntoView()
                                    listState.animateScrollToItem(1)
                                }
                                Toast.makeText(context, "Name required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // FIX: Precise Scroll to Custom Field Row
                            val brokenIdx = viewModel.customFields.indexOfFirst { it.key.isBlank() || it.value.isBlank() }
                            if (brokenIdx != -1) {
                                firstErrorIndex = brokenIdx
                                scope.launch {
                                    // Calculate: Index 4 is the first custom field row
                                    listState.animateScrollToItem(4 + brokenIdx)
                                }
                                Toast.makeText(context, "Incomplete row detected", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val finalPath = if (subCategory.isNotBlank()) "$mainCategory > $subCategory" else mainCategory
                            val currentUri = viewModel.selectedImageUri
                            val newProductId = FirebaseDatabase.getInstance().getReference("users").push().key ?: System.currentTimeMillis().toString()

                            if (currentUri != null) {
                                viewModel.uploadToCloudinary(currentUri) { webUrl ->
                                    viewModel.saveProductToFirebase(newProductId, productName.trim(), finalPath, webUrl) {
                                        navController.popBackStack()
                                    }
                                }
                            } else {
                                viewModel.saveProductToFirebase(newProductId, productName.trim(), finalPath, "") {
                                    navController.popBackStack()
                                }
                            }
                        },
                        enabled = !viewModel.isUploading,
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(isSystemOnline) themeColor else Color.DarkGray,
                            contentColor = DeepMidnight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (viewModel.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = NeonCyan, strokeWidth = 3.dp)
                        } else {
                            Icon(if(isSystemOnline) Icons.Default.CloudUpload else Icons.Default.CloudOff, null)
                            Spacer(Modifier.width(8.dp))
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
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    themeColor: Color = NeonCyan
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = themeColor.copy(0.5f)) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = themeColor.copy(0.6f)) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = themeColor,
            unfocusedBorderColor = themeColor.copy(0.2f),
            focusedContainerColor = SurfaceNavy,
            unfocusedContainerColor = SurfaceNavy,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = themeColor
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
    existingCategories: List<String>,
    themeColor: Color = NeonCyan
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = themeColor.copy(0.6f), style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = currentCategory,
                onValueChange = { onCategorySelected(it) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColor,
                    unfocusedBorderColor = themeColor.copy(0.2f),
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
                modifier = Modifier.background(SurfaceNavy).border(1.dp, themeColor.copy(0.2f), RoundedCornerShape(8.dp))
            ) {
                existingCategories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption, color = Color.White) },
                        onClick = { onCategorySelected(selectionOption); expanded = false }
                    )
                }
            }
        }
    }
}