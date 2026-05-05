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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.data.ProductViewModel
import com.management.inventorypro.ui.theme.DangerRed
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.ui.theme.screens.profile.ProfileCyberField
import com.management.inventorypro.util.ConnectivityObserver


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val database = FirebaseDatabase.getInstance().getReference("User").child(currentUser?.uid ?: "")
    val connectivityObserver = remember { ConnectivityObserver(context) }

    // --- OFFLINE SHIFT LOGIC ---
    val isSystemOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val themeColor = if (isSystemOnline) NeonCyan else DangerRed
    val unselectedColor = if (isSystemOnline) SoftCyan.copy(0.5f) else DangerRed.copy(0.3f)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val email = currentUser?.email ?: "No email linked"
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf("") }


    // Track if we ever successfully got data
    var hasLoadedData by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imageUrl = it.toString() } }

    LaunchedEffect(isSystemOnline) {
        if (isSystemOnline && !hasLoadedData) {
            isLoading = true
            database.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    username = snapshot.child("username").value?.toString() ?: ""
                    phoneNumber = snapshot.child("phone").value?.toString() ?: ""
                    imageUrl = snapshot.child("profileImageUrl").value?.toString() ?: ""
                    hasLoadedData = true
                }
                isLoading = false
            }.addOnFailureListener {
                isLoading = false
            }
        } else if (!isSystemOnline && !hasLoadedData) {
            isLoading = false // Stop spinner so we can show the "No Connection" error
        }
    }

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = { Text("Your Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = themeColor
                ),
                actions = {
                    // Only show edit button if we have data and are online
                    if (isSystemOnline && hasLoadedData) {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(if (isEditing) Icons.Default.Save else Icons.Default.Edit, null, tint = themeColor)
                        }
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
                        icon = { Icon(icon, null, tint = if (isSelected) themeColor else unselectedColor) },
                        label = { Text(label, color = if (isSelected) themeColor else unselectedColor) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = themeColor.copy(0.1f))
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = themeColor)
            }
            // SCENARIO 1: Coming from another screen while offline (No data yet)
            else if (!isSystemOnline && !hasLoadedData) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = DangerRed)
                    Spacer(Modifier.height(16.dp))
                    Text("UPLINK FAILED", color = DangerRed, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(
                        "Profile data could not be retrieved. Please check your connection and try again.",
                        color = Color.White.copy(0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            // SCENARIO 2: Data exists (Already here or loaded), just shift to red
            else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // --- AVATAR (Border shifts to red if offline) ---
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(BorderStroke(2.dp, if (isEditing && isSystemOnline) themeColor else Color.White.copy(0.1f)), CircleShape)
                            .clip(CircleShape)
                            .background(SurfaceNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotEmpty()) {
                            AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = themeColor.copy(0.3f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = username.ifEmpty { "User" }, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = email, fontSize = 14.sp, color = themeColor.copy(0.7f))

                    Spacer(modifier = Modifier.height(40.dp))

                    ProfileCyberField(value = username, onValueChange = { username = it }, label = "Username", enabled = isEditing && isSystemOnline, icon = Icons.Default.Badge, themeColor = themeColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileCyberField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = "Phone Number", enabled = isEditing && isSystemOnline, icon = Icons.Default.Call, themeColor = themeColor)

                    // Optional: Show "Sync Paused" footer if offline
                    if (!isSystemOnline) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("SYSTEM OFFLINE: DATA SYNC PAUSED", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@Composable
fun ProfileCyberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColor: Color // <--- Added this parameter
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SoftCyan.copy(0.4f)) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        // Tint now reacts to the system status
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) themeColor else themeColor.copy(alpha = 0.3f)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = themeColor, // Shifted
            unfocusedBorderColor = Color.White.copy(0.1f),
            disabledBorderColor = Color.White.copy(0.05f),
            focusedContainerColor = SurfaceNavy,
            unfocusedContainerColor = SurfaceNavy,
            disabledContainerColor = SurfaceNavy.copy(0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White.copy(0.6f),
            focusedLabelColor = themeColor // Optional: makes the floating label shift too
        ),
        shape = RoundedCornerShape(12.dp)
    )
}