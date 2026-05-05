package com.management.inventorypro.ui.theme.screens.landing


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        // 1. Start the "Pop" animation
        scale.animateTo(
            targetValue = 0.9f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // 2. Artificial delay so they actually see your cool "Initializing" text
        delay(2000L)

        // 3. Check for existing session
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is remembered! Go to Dashboard
            navController.navigate("dashboard") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // No session found. Go to Landing or Login
            navController.navigate("landing") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(DeepMidnight)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Your Logo here
            Icon(
                imageVector = Icons.Default.Inventory, // Temporary until logo is ready
                contentDescription = "Logo",
                tint = NeonCyan,
                modifier = Modifier.size(120.dp).scale(scale.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "INVENTORY PRO",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
                fontSize = 24.sp
            )

            Text(
                text = "INITIALIZING NEURAL LINK...",
                color = SoftCyan.copy(0.4f),
                fontSize = 10.sp
            )
        }
    }
}