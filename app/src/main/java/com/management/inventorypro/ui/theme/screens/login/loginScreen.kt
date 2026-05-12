package com.management.inventorypro.ui.theme.screens.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.management.inventorypro.data.AuthViewModel
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.util.BiometricHelper

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

    // --- MASTER PREFS & BIOMETRIC CHECK ---
    // This looks at the same "LoginPrefs" file shared with the Settings Screen
    val loginPrefs = remember { context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE) }
    val isBioEnabledInSettings = loginPrefs.getBoolean("bio_enabled", false)
    var rememberMe by remember { mutableStateOf(loginPrefs.getBoolean("remember", false)) }

    val activity = context as? FragmentActivity
    val biometricHelper = remember { activity?.let { BiometricHelper(it) } }

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
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    } else {
                        isLoading = true
                        loginPrefs.edit().putBoolean("remember", rememberMe).apply()
                        authViewModel.login(email, password, navController, context) { success ->
                            if (!success) isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.width(280.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepMidnight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("AUTHENTICATE", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MASTER CONTROLLED BIOMETRIC SECTION ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        // Check if the Master Switch from Settings is ON
                        if (!isBioEnabledInSettings) {
                            Toast.makeText(
                                context,
                                "Biometrics disabled in settings. Manual login required.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // Only run helper if switch is ON in settings
                            biometricHelper?.authenticate { success ->
                                if (success) {
                                    val savedEmail = loginPrefs.getString("saved_email", null)
                                    val savedPass = loginPrefs.getString("saved_password", null)

                                    if (!savedEmail.isNullOrEmpty() && !savedPass.isNullOrEmpty()) {
                                        isLoading = true
                                        authViewModel.login(savedEmail, savedPass, navController, context) { loginSuccess ->
                                            if (!loginSuccess) isLoading = false
                                        }
                                    } else {
                                        Toast.makeText(context, "Manual login required once to save credentials", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric Login",
                    // Visual feedback: Gray when the master switch is off
                    tint = if (isBioEnabledInSettings) NeonCyan else Color.Gray,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "BIOMETRIC LOGIN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isBioEnabledInSettings) NeonCyan.copy(alpha = 0.7f) else Color.Gray,
                    letterSpacing = 1.sp
                )
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

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan, strokeWidth = 4.dp)
            }
        }
    }

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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.sendPasswordReset(resetEmail, {
                        showResetDialog = false
                        Toast.makeText(context, "Reset link sent!", Toast.LENGTH_SHORT).show()
                    }, { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    })
                }) { Text("SEND", color = NeonCyan) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = Color.White.copy(0.5f))
                }
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
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = SoftCyan.copy(0.4f)) },
        modifier = Modifier.width(280.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = NeonCyan.copy(0.7f)) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
                        tint = NeonCyan.copy(alpha = 0.5f)
                    )
                }
            }
        },
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