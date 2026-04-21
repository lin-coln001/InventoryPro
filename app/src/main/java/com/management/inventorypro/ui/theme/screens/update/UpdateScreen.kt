package com.management.inventorypro.ui.theme.screens.update

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.models.ProductModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(navController: NavController, productId: String?) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("users")
        .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
        .child("inventory")
        .child(productId ?: "")

    // --- STATE MANAGEMENT ---
    var name by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    val customFields = remember { mutableStateListOf<Pair<String, String>>() }
    var isLoading by remember { mutableStateOf(true) }

    // --- IMAGE PICKER LAUNCHER ---
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUrl = it.toString() }
    }

    // --- FETCH EXISTING DATA ---
    LaunchedEffect(Unit) {
        database.get().addOnSuccessListener { snapshot ->
            val product = snapshot.getValue(ProductModel::class.java)
            product?.let {
                name = it.name
                imageUrl = it.imageUrl
                customFields.clear()
                it.customFields.forEach { (key, value) ->
                    customFields.add(key to value)
                }
            }
            isLoading = false
        }.addOnFailureListener {
            Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- IMAGE SECTION ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(contentAlignment = Alignment.TopEnd) {
                        if (imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Item Image",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                    .clickable { launcher.launch("image/*") }
                            )
                            // Red "X" button to remove image
                            IconButton(
                                onClick = { imageUrl = "" },
                                modifier = Modifier
                                    .offset(x = 10.dp, y = (-10).dp)
                                    .background(Color.Red, CircleShape)
                                    .size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            // "Add Image" Placeholder
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .border(2.dp, Color.Gray, RoundedCornerShape(12.dp))
                                    .clickable { launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(40.dp))
                                    Text("Add Image", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- PRODUCT NAME ---
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // FIX: Instead of .align(Alignment.Start), wrap it in a Box or Row
                    // OR just use fillMaxWidth() to allow the text to sit at the start by default
                    Text(
                        text = "Further details",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth(), // This fills the width
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start // This forces start alignment
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- DYNAMIC CUSTOM FIELDS ---
                itemsIndexed(customFields) { index, field ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = field.first,
                            onValueChange = { customFields[index] = it to field.second },
                            label = { Text("Label") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = field.second,
                            onValueChange = { customFields[index] = field.first to it },
                            label = { Text("Value") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // --- ACTION BUTTONS ---
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val updatedProduct = ProductModel(
                                id = productId ?: "",
                                name = name,
                                imageUrl = imageUrl,
                                customFields = customFields.toMap()
                            )
                            database.setValue(updatedProduct).addOnSuccessListener {
                                Toast.makeText(context, "Product Updated", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Changes")
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}