package com.management.inventorypro.ui.theme.screens.register

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

// Final Consistency Palette
val DeepMidnight = Color(0xFF0A0E1A)
val SurfaceNavy = Color(0xFF161C2C)
val NeonCyan = Color(0xFF00E5FF)
val SoftCyan = Color(0xFFB2EBF2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmpassword by remember { mutableStateOf("") }

    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
        }.build()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(DeepMidnight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- WELCOME ANIMATION ---
        AsyncImage(
            model = R.drawable.welcome_aboard,
            imageLoader = imageLoader,
            contentDescription = "Register Animation",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "NEW PROTOCOL",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NeonCyan,
            letterSpacing = 4.sp
        )
        Text(
            text = "Create your System Identity",
            fontSize = 12.sp,
            color = SoftCyan.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- REGISTRATION FIELDS ---
        RegisterCyberField(username, { username = it }, "Username", Icons.Default.Person)
        Spacer(modifier = Modifier.height(8.dp))

        RegisterCyberField(phone, { phone = it }, "Comms Number", Icons.Default.Phone)
        Spacer(modifier = Modifier.height(8.dp))

        RegisterCyberField(email, { email = it }, "Email Address", Icons.Default.Email)
        Spacer(modifier = Modifier.height(8.dp))

        RegisterCyberField(password, { password = it }, "Secure Key", Icons.Default.Lock, true)
        Spacer(modifier = Modifier.height(8.dp))

        RegisterCyberField(confirmpassword, { confirmpassword = it }, "Confirm Key", Icons.Default.Check, true)

        Spacer(modifier = Modifier.height(32.dp))

        // --- REGISTER BUTTON ---
        Button(
            onClick = {
                authViewModel.signup(
                    username, phone, email, password, confirmpassword, navController, context
                )
            },
            modifier = Modifier
                .width(280.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepMidnight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("INITIALIZE ACCOUNT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- FOOTER ---
        Row {
            Text(text = "Existing User? ", color = Color.White.copy(0.6f))
            Text(
                text = "Back to Login",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("login") }
            )
        }

        // Final spacer to ensure nothing is cut off by the edge of the screen
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RegisterCyberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SoftCyan.copy(0.4f), fontSize = 12.sp) },
        modifier = Modifier.width(280.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = NeonCyan.copy(0.6f), modifier = Modifier.size(20.dp)) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color.White.copy(0.05f),
            focusedContainerColor = SurfaceNavy,
            unfocusedContainerColor = SurfaceNavy,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = NeonCyan
        ),
        shape = RoundedCornerShape(12.dp)
    )
}