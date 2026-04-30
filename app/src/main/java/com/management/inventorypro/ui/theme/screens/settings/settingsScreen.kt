package com.management.inventorypro.ui.theme.screens.settings

import android.content.Context // Added for SharedPreferences
import androidx.compose.foundation.BorderStroke // Added for styling
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Added to get context
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance()
    val context = LocalContext.current // To access SharedPreferences
    val sharedPref = remember { context.getSharedPreferences("InventoryPrefs", Context.MODE_PRIVATE) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showAll by remember { mutableStateOf(true) }
    var fieldCountText by remember { mutableStateOf("2") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid != null) {
            database.getReference("users").child(uid).child("settings").child("maxVisibleFields")
                .get().addOnSuccessListener { snapshot ->
                    val value = snapshot.getValue(Int::class.java) ?: 0
                    if (value == 0) {
                        showAll = true
                    } else {
                        showAll = false
                        fieldCountText = value.toString()
                    }
                }
        }
    }

    Scaffold(
        containerColor = DeepMidnight,
        topBar = {
            TopAppBar(
                title = { Text("System Configuration", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = NeonCyan
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceNavy,
                tonalElevation = 0.dp
            ) {
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DeepMidnight)
                .padding(24.dp)
        ) {
            Text(
                text = "Display Preferences",
                color = SoftCyan.copy(0.6f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Toggle Card
            Surface(
                color = SurfaceNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Limitless View", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Show all custom fields in list", color = SoftCyan.copy(0.5f), fontSize = 12.sp)
                    }
                    Switch(
                        checked = showAll,
                        onCheckedChange = { showAll = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(0.3f),
                            uncheckedThumbColor = SoftCyan.copy(0.5f),
                            uncheckedTrackColor = SurfaceNavy
                        )
                    )
                }
            }

            // Specific Number Input
            if (!showAll) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = fieldCountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) fieldCountText = it },
                    label = { Text("Visible Field Limit") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color.White.copy(0.1f),
                        focusedContainerColor = SurfaceNavy,
                        unfocusedContainerColor = SurfaceNavy,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- NEW: RECALIBRATE SYSTEM CARD ---
            Text(
                text = "Maintenance",
                color = SoftCyan.copy(0.6f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                color = SurfaceNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System Calibration", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Re-run the setup survey",
                            color = SoftCyan.copy(0.5f),
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = {
                            // RESET THE FLAG
                            sharedPref.edit().putBoolean("first_run", true).apply()
                            // HEAD BACK TO DASHBOARD
                            navController.navigate("dashboard") {
                                popUpTo("settings") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan.copy(alpha = 0.1f),
                            contentColor = NeonCyan
                        ),
                        border = BorderStroke(1.dp, NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("RE-RUN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (uid != null) {
                        isSaving = true
                        val finalValue = if (showAll) 0 else fieldCountText.toIntOrNull() ?: 2
                        database.getReference("users").child(uid).child("settings")
                            .child("maxVisibleFields").setValue(finalValue)
                            .addOnSuccessListener {
                                isSaving = false
                                navController.popBackStack()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DeepMidnight
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepMidnight)
                } else {
                    Text("SAVE CHANGES", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}