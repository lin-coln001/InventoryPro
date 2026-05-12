package com.management.inventorypro.ui.theme.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.ui.theme.DangerRed
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.util.BiometricHelper
import com.management.inventorypro.util.ConnectivityObserver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance()
    val context = LocalContext.current

    // Preferences
    val sharedPref = remember { context.getSharedPreferences("InventoryPrefs", Context.MODE_PRIVATE) }
    val loginPrefs = remember { context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE) }

    // Biometric Setup
    val activity = context as? FragmentActivity
    val biometricHelper = remember { activity?.let { BiometricHelper(it) } }
    var isBioEnabled by remember { mutableStateOf(loginPrefs.getBoolean("bio_enabled", false)) }

    // Connectivity
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isSystemOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val themeColor = if (isSystemOnline) NeonCyan else DangerRed

    // Theme Variables
    val labelColor = if (isSystemOnline) SoftCyan.copy(0.6f) else DangerRed.copy(0.7f)
    val secondaryTextColor = if (isSystemOnline) SoftCyan.copy(0.5f) else DangerRed.copy(0.5f)
    val cardBorderOpacity = if (isSystemOnline) 0.05f else 0.4f

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showAll by remember { mutableStateOf(true) }
    var fieldCountText by remember { mutableStateOf("2") }
    var isSaving by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
                title = {
                    Text(
                        if (isSystemOnline) "System Configuration" else "CONFIG: CONNECTION LOST",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepMidnight,
                    titleContentColor = themeColor
                )
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
                    val unselectedColor = if (isSystemOnline) SoftCyan.copy(0.5f) else DangerRed.copy(0.3f)
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(DeepMidnight)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // --- DISPLAY SECTION ---
            Text(
                text = "Display Preferences".uppercase(),
                color = labelColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                color = SurfaceNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, themeColor.copy(alpha = cardBorderOpacity))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Limitless View", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Show all custom fields in list", color = secondaryTextColor, fontSize = 12.sp)
                    }
                    Switch(
                        checked = showAll,
                        onCheckedChange = { if (isSystemOnline) showAll = it },
                        enabled = isSystemOnline,
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColor)
                    )
                }
            }

            if (!showAll) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = fieldCountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) fieldCountText = it },
                    label = { Text("Visible Field Limit") },
                    enabled = isSystemOnline,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = Color.White.copy(0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECURITY SECTION ---
            Text(
                text = "Security Protocols".uppercase(),
                color = labelColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                color = SurfaceNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, themeColor.copy(alpha = cardBorderOpacity))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = themeColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Biometric Access", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Touch ID for quick entry", color = secondaryTextColor, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isBioEnabled,
                        onCheckedChange = { newState ->
                            // Note: 'newState' is captured here from the onCheckedChange
                            biometricHelper?.authenticate { success ->
                                if (success) {
                                    isBioEnabled = newState
                                    loginPrefs.edit().putBoolean("bio_enabled", newState).apply()
                                    Toast.makeText(context, "Security Updated", Toast.LENGTH_SHORT).show()
                                } else {
                                    // If they cancel, the switch stays where it was
                                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = isSystemOnline,
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColor)
                    )

                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- MAINTENANCE SECTION ---
            Text(
                text = "Maintenance".uppercase(),
                color = labelColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                color = SurfaceNavy,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, themeColor.copy(alpha = cardBorderOpacity))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System Calibration", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Re-run the setup survey", color = secondaryTextColor, fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            sharedPref.edit().putBoolean("first_run", true).apply()
                            navController.navigate("dashboard") { popUpTo("settings") { inclusive = true } }
                        },
                        enabled = isSystemOnline,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSystemOnline) themeColor.copy(0.1f) else Color.Transparent,
                            contentColor = if (isSystemOnline) themeColor else Color.Gray
                        ),
                        border = BorderStroke(1.dp, if (isSystemOnline) themeColor else Color.Gray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RE-RUN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    if (isSystemOnline) {
                        if (uid != null) {
                            isSaving = true
                            val finalValue = if (showAll) 0 else fieldCountText.toIntOrNull() ?: 2
                            database.getReference("users").child(uid).child("settings")
                                .child("maxVisibleFields").setValue(finalValue)
                                .addOnSuccessListener {
                                    isSaving = false
                                    Toast.makeText(context, "System Optimized", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                        }
                    } else {
                        Toast.makeText(context, "ERROR: Action requires active uplink", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSystemOnline) themeColor else Color(0xFF222222),
                    contentColor = if (isSystemOnline) DeepMidnight else Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepMidnight)
                } else {
                    Text(
                        text = if (isSystemOnline) "SAVE CHANGES" else "OFFLINE: SAVING DISABLED",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}