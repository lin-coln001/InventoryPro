package com.management.inventorypro.ui.theme.screens.dashboard

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.management.inventorypro.data.AuthViewModel
import com.management.inventorypro.models.inventoryQuestions
import com.management.inventorypro.ui.theme.*
import com.management.inventorypro.util.isOnline
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    // 1. STATE MANAGEMENT
    val sharedPref = remember { context.getSharedPreferences("InventoryPrefs", Context.MODE_PRIVATE) }
    var showSurvey by remember { mutableStateOf(sharedPref.getBoolean("first_run", true)) }

    // Connectivity State
    var isSystemOnline by remember { mutableStateOf(isOnline(context)) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var username by remember { mutableStateOf("User") }
    var itemCount by remember { mutableStateOf(0) }

    // 2. DYNAMIC THEMING
    val themeColor = if (isSystemOnline) NeonCyan else DangerRed

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 3. EFFECTS
    LaunchedEffect(Unit) {
        authViewModel.getUsername { username = it }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val database = FirebaseDatabase.getInstance().getReference("users").child(uid).child("inventory")
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    itemCount = snapshot.childrenCount.toInt()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    // Monitoring Connection
    LaunchedEffect(Unit) {
        while(true) {
            isSystemOnline = isOnline(context)
            delay(3000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DeepMidnight,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("InventoryPro", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                            Text(
                                text = if (isSystemOnline) "LINK ACTIVE" else "CONNECTION SEVERED",
                                fontSize = 10.sp,
                                color = themeColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DeepMidnight,
                        titleContentColor = themeColor
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
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    // Use themeColor here (NeonCyan or DangerRed)
                                    tint = if (isSelected) themeColor else themeColor.copy(0.4f)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    color = if (isSelected) themeColor else themeColor.copy(0.4f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                // This is the "pill" shape behind the selected icon
                                indicatorColor = themeColor.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(DeepMidnight)
                    .padding(16.dp)
            ) {
                // Welcome Header
                Text(
                    text = if (isSystemOnline) "Welcome back $username" else "SYSTEM OFFLINE",
                    color = if (isSystemOnline) OffWhite else DangerRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )

                // Dynamic Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(value = "$itemCount", label = "Items", color = themeColor, modifier = Modifier.weight(1f))
                    StatCard(value = if (isSystemOnline) "Live" else "Offline", label = "Status", color = themeColor, modifier = Modifier.weight(1f))
                }

                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.labelLarge,
                    color = themeColor.copy(0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ActionCard(
                    title = "Add New Item",
                    subtitle = if (isSystemOnline) "Register product to database" else "Sync Required to Proceed",
                    icon = Icons.Filled.Inventory,
                    iconColor = themeColor,
                    onClick = { if (isSystemOnline) navController.navigate("add_product") }
                )

                ActionCard(
                    title = "View Inventory",
                    subtitle = if (isSystemOnline) "Check current stock levels" else "Local cache unavailable",
                    icon = Icons.Filled.List,
                    iconColor = themeColor,
                    onClick = { if (isSystemOnline) navController.navigate("view_inventory") }
                )
            }
        }

        // SURVEY (Stays Cyan as per your request)
        if (showSurvey) {
            OnboardingSurvey(
                onComplete = {
                    sharedPref.edit().putBoolean("first_run", false).apply()
                    showSurvey = false
                    scope.launch {
                        snackbarHostState.showSnackbar("SYSTEM OPTIMIZED: Engine Ready")
                    }
                },
                onSkip = {
                    sharedPref.edit().putBoolean("first_run", false).apply()
                    showSurvey = false
                }
            )
        }
    }
}

@Composable
fun OnboardingSurvey(onComplete: () -> Unit, onSkip: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(-1) }
    val selectedAnswers = remember { mutableStateListOf<Int?>(null, null, null, null, null) }
    val progress by animateFloatAsState(targetValue = if (currentStep >= 0) (currentStep + 1) / 5f else 0f)

    if (currentStep == -1) {
        AlertDialog(
            containerColor = SurfaceNavy,
            onDismissRequest = onSkip,
            title = { Text("Personalize System", color = NeonCyan, fontWeight = FontWeight.Bold) },
            text = { Text("Configure your inventory environment with 5 quick questions.", color = Color.White) },
            confirmButton = { TextButton(onClick = { currentStep = 0 }) { Text("START", color = NeonCyan) } },
            dismissButton = { TextButton(onClick = onSkip) { Text("SKIP", color = Color.White.copy(0.5f)) } }
        )
    } else if (currentStep < inventoryQuestions.size) {
        val q = inventoryQuestions[currentStep]
        AlertDialog(
            containerColor = SurfaceNavy,
            onDismissRequest = { },
            title = {
                Column {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = NeonCyan,
                        trackColor = Color.White.copy(0.1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("STEP ${currentStep + 1}/5", fontSize = 12.sp, color = SoftCyan)
                }
            },
            text = {
                Column {
                    Text(q.question, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    q.options.forEachIndexed { index, option ->
                        val isSel = selectedAnswers[currentStep] == index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth().padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) NeonCyan.copy(0.1f) else Color.Transparent)
                                .border(1.dp, if (isSel) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedAnswers[currentStep] = index }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSel, onClick = { selectedAnswers[currentStep] = index }, colors = RadioButtonDefaults.colors(selectedColor = NeonCyan))
                            Text(option, color = if (isSel) NeonCyan else Color.White, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedAnswers[currentStep] != null,
                    onClick = { if (currentStep == 4) onComplete() else currentStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) { Text(if (currentStep == 4) "FINISH" else "NEXT", color = DeepMidnight) }
            },
            dismissButton = {
                TextButton(onClick = { if (currentStep == 0) currentStep = -1 else currentStep-- }) {
                    Text("BACK", color = Color.White.copy(0.6f))
                }
            }
        )
    }
}

@Composable
fun StatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = color.copy(0.7f), fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 12.sp, color = iconColor.copy(0.6f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = iconColor.copy(0.3f), modifier = Modifier.size(16.dp))
        }
    }
}