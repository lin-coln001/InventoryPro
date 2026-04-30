package com.management.inventorypro.ui.theme.screens.dashboard


import android.content.Context
import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Brush
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


import kotlinx.coroutines.launch

// --- SURVEY DATA ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    val sharedPref = remember { context.getSharedPreferences("InventoryPrefs", Context.MODE_PRIVATE) }
    var showSurvey by remember { mutableStateOf(sharedPref.getBoolean("first_run", true)) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var username by remember { mutableStateOf("User") }
    var itemCount by remember { mutableStateOf(0) }

    // Snackbar Logic
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DeepMidnight,
            // --- ADDED SNACKBAR HOST ---
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                        border = BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Text(
                            text = data.visuals.message,
                            color = NeonCyan,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = { Text("InventoryPro", fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepMidnight, titleContentColor = NeonCyan),
                    actions = {
                        TextButton(
                            onClick = { authViewModel.logout(navController, context) },
                            colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                        ) { Text("LOGOUT", fontWeight = FontWeight.Bold) }
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
                            icon = { Icon(icon, contentDescription = label, tint = if (isSelected) NeonCyan else SoftCyan.copy(0.5f)) },
                            label = { Text(label, color = if (isSelected) NeonCyan else SoftCyan.copy(0.5f)) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = NeonCyan.copy(0.15f))
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
                Text(
                    text = if (username == "...") "INITIALIZING..." else "Welcome back $username",
                    color = if (username == "...") SoftCyan.copy(0.3f) else OffWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(value = "$itemCount", label = "Items", modifier = Modifier.weight(1f))
                    StatCard(value = "Live", label = "Status", modifier = Modifier.weight(1f))
                }

                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.labelLarge,
                    color = SoftCyan.copy(0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ActionCard(
                    title = "Add New Item",
                    subtitle = "Register product to database",
                    icon = Icons.Filled.Inventory,
                    onClick = { navController.navigate("add_product") }
                )

                ActionCard(
                    title = "View Inventory",
                    subtitle = "Check current stock levels",
                    icon = Icons.Filled.List,
                    onClick = { navController.navigate("view_inventory") }
                )
            }
        }

        if (showSurvey) {
            OnboardingSurvey(
                onComplete = {
                    sharedPref.edit().putBoolean("first_run", false).apply()
                    showSurvey = false
                    scope.launch {
                        snackbarHostState.showSnackbar("SYSTEM OPTIMIZED: Inventory Engine Ready")
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
    fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier.height(100.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = value, color = NeonCyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = label, color = SoftCyan.copy(0.7f), fontSize = 12.sp)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = subtitle, fontSize = 12.sp, color = SoftCyan.copy(0.6f))
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowForwardIos, contentDescription = null, tint = SoftCyan.copy(0.3f), modifier = Modifier.size(16.dp))
            }
        }
    }

