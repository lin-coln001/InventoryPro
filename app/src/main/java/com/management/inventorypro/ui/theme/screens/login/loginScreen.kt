package com.management.inventorypro.ui.theme.screens.login

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE) }
    var rememberMe by remember { mutableStateOf(sharedPref.getBoolean("remember", false)) }

    Box(modifier = Modifier.fillMaxSize().background(DeepMidnight)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(280.dp).padding(vertical = 12.dp)
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

            Button(
                onClick = {
                    isLoading = true
                    sharedPref.edit().putBoolean("remember", rememberMe).apply()
                    authViewModel.login(email, password, navController, context)
                    // Note: If login fails, ensure your ViewModel or a side effect sets isLoading = false
                },
                enabled = !isLoading,
                modifier = Modifier.width(280.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepMidnight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("AUTHENTICATE", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Text(text = "New User? ", color = Color.White.copy(0.7f))
                Text(
                    text = "Register ",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }

            Text(
                text = "Forgot Password?",
                color = NeonCyan.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.clickable { showResetDialog = true }.padding(16.dp)
            )
        }

        // --- NEON LOADING OVERLAY ---
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan, strokeWidth = 4.dp)
            }
        }
    }

    // Reset Dialog Logic
    if (showResetDialog) {
        AlertDialog(
            containerColor = SurfaceNavy,
            onDismissRequest = { showResetDialog = false },
            title = { Text("RECOVER ACCESS", color = NeonCyan, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    label = { Text("Email", color = SoftCyan.copy(0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, focusedTextColor = Color.White)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.sendPasswordReset(resetEmail, { showResetDialog = false }, {})
                }) { Text("SEND", color = NeonCyan) }
            }
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
    // Local state to toggle visibility
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SoftCyan.copy(0.4f)) },
        modifier = Modifier.width(280.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = NeonCyan.copy(0.7f)) },

        // --- ADDED TRAILING ICON FOR VISIBILITY TOGGLE ---
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = NeonCyan.copy(alpha = 0.5f)
                    )
                }
            }
        },

        // --- ADDED TRANSFORMATION LOGIC ---
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,

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