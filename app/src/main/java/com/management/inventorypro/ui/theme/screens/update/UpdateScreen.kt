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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import com.management.inventorypro.ui.theme.*
import com.management.inventorypro.ui.theme.screens.add.CategorySelector
import com.management.inventorypro.util.ConnectivityObserver

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    navController: NavController,
    productId: String?,
    viewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()


    val connectivityObserver = remember { ConnectivityObserver(context) }


    val isSystemOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    var firstErrorIndex by remember { mutableStateOf<Int?>(null) }
    var nameHasError by remember { mutableStateOf(false) }
    val themeColor = if (isSystemOnline) NeonCyan else DangerRed

    val allProducts by viewModel.products.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val database = FirebaseDatabase.getInstance().getReference("users")
        .child(userId).child("inventory").child(productId ?: "")

    var productName by remember { mutableStateOf("") }
    var mainCategory by remember { mutableStateOf("Uncategorized") }
    var subCategory by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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

    // Fetch initial data
    LaunchedEffect(Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val product = snapshot.getValue(ProductModel::class.java)
            product?.let {
                productName = it.name
                imageUrl = it.imageUrl
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
                title = {
                    Column {
                        Text("Update Item", fontWeight = FontWeight.Bold)
                        if (!isSystemOnline) {
                            Text("OFFLINE - UPDATE LOCKED", fontSize = 10.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = themeColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = themeColor
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
                        onClick = { if (isSystemOnline) showDeleteConfirmation = true else Toast.makeText(context, "Uplink required to delete", Toast.LENGTH_SHORT).show() },
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
                            if (!isSystemOnline) {
                                Toast.makeText(context, "YOU ARE OFFLINE: go online to continue", Toast.LENGTH_SHORT).show()
                                return@Button
                            }


                            if (productName.trim().isBlank()) {
                                nameHasError = true
                                scope.launch { listState.animateScrollToItem(1) }
                                Toast.makeText(context, "Item name required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }


                            val brokenIdx = viewModel.customFields.indexOfFirst { it.key.isBlank() || it.value.isBlank() }
                            if (brokenIdx != -1) {
                                firstErrorIndex = brokenIdx
                                scope.launch { listState.animateScrollToItem(brokenIdx + 5) }
                                Toast.makeText(context, "Empty  row detected", Toast.LENGTH_SHORT).show()
                                return@Button
                            }


                            val finalPath = if (subCategory.isNotBlank()) "$mainCategory > $subCategory" else mainCategory
                            val currentUri = viewModel.selectedImageUri

                            val saveAction = { finalImageUrl: String ->
                                viewModel.saveProductToFirebase(
                                    productId = productId ?: "",
                                    name = productName.trim(),
                                    category = finalPath,
                                    imageUrl = finalImageUrl,
                                    onComplete = {
                                        Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT).show()
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
                        },
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(isSystemOnline) themeColor else Color.DarkGray,
                            contentColor = DeepMidnight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !viewModel.isUploading
                    ) {
                        if (viewModel.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NeonCyan)
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
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

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
                        val displayUri = viewModel.selectedImageUri ?: if (imageUrl.isNotEmpty()) Uri.parse(imageUrl) else null
                        if (displayUri != null) {
                            AsyncImage(
                                model = displayUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, tint = themeColor)
                        }
                    }
                }
            }

            item {
                UpdateCyberTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        if(it.isNotBlank()) nameHasError = false
                    },
                    label = "Item Name",
                    themeColor = if (nameHasError) DangerRed else themeColor
                )
            }


            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Categorisation", color = themeColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    CategorySelector(
                        label = "Main Category",
                        currentCategory = mainCategory,
                        onCategorySelected = { mainCategory = it; subCategory = "" },
                        existingCategories = dynamicMainCategories,
                        themeColor = themeColor
                    )

                    CategorySelector(
                        label = "Sub-Category (Optional)",
                        currentCategory = subCategory,
                        onCategorySelected = { subCategory = it },
                        existingCategories = dynamicSubCategories,
                        themeColor = themeColor
                    )
                }
            }


            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Advanced Metadata", color = themeColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        TextButton(onClick = { viewModel.addNewField() }, colors = ButtonDefaults.textButtonColors(contentColor = themeColor)) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Add Field")
                        }
                    }
                    if (firstErrorIndex != null) {
                        Text("⚠️ ALERT: Incomplete data at row ${firstErrorIndex!! + 1}", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            itemsIndexed(viewModel.customFields) { index, field ->
                val isBroken = index == firstErrorIndex
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    UpdateCyberTextField(
                        value = field.key,
                        onValueChange = {
                            viewModel.customFields[index] = field.copy(key = it)
                            if(it.isNotBlank()) firstErrorIndex = null
                        },
                        label = "Label",
                        modifier = Modifier.weight(1f),
                        themeColor = if(isBroken && field.key.isBlank()) DangerRed else themeColor
                    )
                    UpdateCyberTextField(
                        value = field.value,
                        onValueChange = {
                            viewModel.customFields[index] = field.copy(value = it)
                            if(it.isNotBlank()) firstErrorIndex = null
                        },
                        label = "Value",
                        modifier = Modifier.weight(1f),
                        themeColor = if(isBroken && field.value.isBlank()) DangerRed else themeColor
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
                titleContentColor = themeColor,
                textContentColor = Color.White,
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Entry") },
                text = { Text("Permanently wipe '$productName' from the cloud database?") },
                confirmButton = {
                    TextButton(onClick = {
                        database.removeValue().addOnSuccessListener {
                            navController.popBackStack()
                        }
                    }) { Text("WIPE DATA", color = DangerRed, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) { Text("ABORT", color = SoftCyan) }
                }
            )
        }
    }
}

@Composable
fun UpdateCyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    themeColor: Color = NeonCyan
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = themeColor.copy(0.5f)) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = themeColor,
            unfocusedBorderColor = themeColor.copy(0.1f),
            focusedContainerColor = SurfaceNavy,
            unfocusedContainerColor = SurfaceNavy,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = themeColor
        ),
        shape = RoundedCornerShape(12.dp)
    )
}