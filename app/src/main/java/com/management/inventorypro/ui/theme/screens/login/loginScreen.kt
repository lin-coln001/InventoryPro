package com.management.inventorypro.ui.theme.screens.login

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.management.inventorypro.R
import com.management.inventorypro.data.AuthViewModel
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.ui.theme.screens.login.LoginCyberField

// Consistency Palette
//val DeepMidnight = Color(0xFF0A0E1A)
//val SurfaceNavy = Color(0xFF161C2C)
//val NeonCyan = Color(0xFF00E5FF)
//val SoftCyan = Color(0xFFB2EBF2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
        }.build()

    val sharedPref = remember { context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE) }
    var rememberMe by remember { mutableStateOf(sharedPref.getBoolean("remember", false)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepMidnight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- LOGO / GIF SECTION ---
//        AsyncImage(
//            model = R.drawable.hello,
//            imageLoader = imageLoader,
//            contentDescription = "Login Animation",
//            modifier = Modifier
//                .size(160.dp)
//                .clip(RoundedCornerShape(20.dp)),
//            contentScale = ContentScale.Fit
//        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SYSTEM ACCESS",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NeonCyan,
            letterSpacing = 4.sp
        )
        Text(
            text = "Enter Credentials to Proceed",
            fontSize = 12.sp,
            color = SoftCyan.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- INPUT FIELDS ---
            LoginCyberField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            icon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginCyberField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            icon = Icons.Default.Lock,
            isPassword = true
        )

        // --- REMEMBER ME ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(280.dp)
                .padding(vertical = 12.dp)
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonCyan,
                    uncheckedColor = SoftCyan.copy(alpha = 0.4f),
                    checkmarkColor = DeepMidnight
                )
            )
            Text(
                text = "Keep Session Active",
                fontSize = 14.sp,
                color = SoftCyan.copy(alpha = 0.7f),
                modifier = Modifier.clickable { rememberMe = !rememberMe }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- LOGIN BUTTON ---
        Button(
            onClick = {
                sharedPref.edit().putBoolean("remember", rememberMe).apply()
                authViewModel.login(email, password, navController, context)
            },
            modifier = Modifier
                .width(280.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = DeepMidnight
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("AUTHENTICATE", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- FOOTER ---
        Row {
            Text(text = "New User? ", color = Color.White.copy(0.7f))
            Text(
                text = "Register ",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("register") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Forgot password?",
            color = SoftCyan.copy(0.4f),
            fontSize = 12.sp,
            modifier = Modifier.clickable { /* Handle forgot password */ }
        )
    }
}

@Composable
fun LoginCyberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SoftCyan.copy(0.4f)) },
        modifier = Modifier.width(280.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = NeonCyan.copy(0.7f)) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color.White.copy(0.1f),
            focusedContainerColor = SurfaceNavy,
            unfocusedContainerColor = SurfaceNavy,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = NeonCyan
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
