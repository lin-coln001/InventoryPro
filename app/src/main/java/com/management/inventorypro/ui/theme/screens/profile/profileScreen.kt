package com.management.inventorypro.ui.theme.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// Consistency Palette
val DeepMidnight = Color(0xFF0A0E1A)
val SurfaceNavy = Color(0xFF161C2C)
val NeonCyan = Color(0xFF00E5FF)
val SoftCyan = Color(0xFFB2EBF2)
val DangerRed = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val database = FirebaseDatabase.getInstance().getReference("User").child(currentUser?.uid ?: "")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- STATE ---
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val email = currentUser?.email ?: "No email linked"
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imageUrl = it.toString() } }

    LaunchedEffect(Unit) {
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                username = snapshot.child("username").value?.toString() ?: ""
                phoneNumber = snapshot.child("phone").value?.toString() ?: ""
                imageUrl = snapshot.child("profileImageUrl").value?.toString() ?: ""
            }
            isLoading = false
        }
    }

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = { Text("Agent Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = NeonCyan
                ),
                actions = {
                    IconButton(onClick = {
                        if (isEditing) {
                            val updates = mapOf(
                                "username" to username,
                                "phone" to phoneNumber,
                                "profileImageUrl" to imageUrl
                            )
                            database.updateChildren(updates).addOnSuccessListener {
                                Toast.makeText(context, "System Updated", Toast.LENGTH_SHORT).show()
                                isEditing = false
                            }
                        } else {
                            isEditing = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (isEditing) NeonCyan else SoftCyan.copy(0.7f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceNavy, tonalElevation = 0.dp) {
                val navItems = listOf(
                    Triple("dashboard", Icons.Filled.Home, "Home"),
                    Triple("settings", Icons.Filled.Settings, "Settings"),
                    Triple("tips", Icons.Filled.Lightbulb, "Tips"),
                    Triple("profile", Icons.Filled.Person, "Profile")
                )

                navItems.forEach { (route, icon, label) ->
                    val isSelected = currentRoute == route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            selectedTextColor = NeonCyan,
                            unselectedIconColor = SoftCyan.copy(0.5f),
                            unselectedTextColor = SoftCyan.copy(0.5f),
                            indicatorColor = NeonCyan.copy(0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().background(DeepMidnight), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // --- PROFILE AVATAR WITH GLOW ---
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(BorderStroke(2.dp, if (isEditing) NeonCyan else Color.White.copy(0.1f)), CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(SurfaceNavy)
                        .clickable(enabled = isEditing) { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = SoftCyan.copy(0.3f))
                    }
                    if (isEditing) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = NeonCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = if (username.isEmpty()) "Unknown Agent" else username, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = email, fontSize = 14.sp, color = NeonCyan.copy(0.7f), letterSpacing = 1.sp)

                Spacer(modifier = Modifier.height(40.dp))

                // --- SYSTEM INPUTS ---
                ProfileCyberField(value = username, onValueChange = { username = it }, label = "Identifier", enabled = isEditing, icon = Icons.Default.Badge)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileCyberField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = "Comms Line", enabled = isEditing, icon = Icons.Default.Call)

                Spacer(modifier = Modifier.weight(1f))

                // --- LOGOUT BUTTON ---
                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        navController.navigate("login") { popUpTo(0) }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = BorderStroke(1.dp, DangerRed.copy(0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TERMINATE SESSION", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileCyberField(value: String, onValueChange: (String) -> Unit, label: String, enabled: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SoftCyan.copy(0.4f)) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, contentDescription = null, tint = if (enabled) NeonCyan else SoftCyan.copy(0.3f)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color.White.copy(0.1f),
            disabledBorderColor = Color.White.copy(0.05f),
            focusedContainerColor = SurfaceNavy,
            unfocusedContainerColor = SurfaceNavy,
            disabledContainerColor = SurfaceNavy.copy(0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White.copy(0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}