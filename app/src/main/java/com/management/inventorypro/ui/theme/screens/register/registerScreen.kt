package com.management.inventorypro.ui.theme.screens.register

import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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

import com.management.inventorypro.data.AuthViewModel
import com.management.inventorypro.ui.theme.DeepMidnight
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmpassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(DeepMidnight)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "REGISTER HERE",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonCyan,
                letterSpacing = 4.sp
            )
            Text(
                text = "Create your inventory management account",
                fontSize = 12.sp,
                color = SoftCyan.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LoginCyberField(username, { username = it }, "Username", Icons.Default.Person)
            Spacer(modifier = Modifier.height(8.dp))
            LoginCyberField(phone, { phone = it }, "Phone Number", Icons.Default.Phone)
            Spacer(modifier = Modifier.height(8.dp))
            LoginCyberField(email, { email = it }, "Email Address", Icons.Default.Email)
            Spacer(modifier = Modifier.height(8.dp))
            LoginCyberField(password, { password = it }, "Password", Icons.Default.Lock, true)
            Spacer(modifier = Modifier.height(8.dp))
            LoginCyberField(confirmpassword, { confirmpassword = it }, "Confirm password", Icons.Default.Check, true)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {if (username.isBlank() || phone.isBlank() || email.isBlank() ||
                    password.isBlank() || confirmpassword.isBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else if (password != confirmpassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    isLoading = true
                    authViewModel.signup(username, phone, email, password, confirmpassword, navController, context)
                }
                },
                enabled = !isLoading,
                modifier = Modifier.width(280.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepMidnight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CREATE ACCOUNT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Text(text = "Existing User? ", color = Color.White.copy(0.6f))
                Text(
                    text = " Login",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("login") }
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan, strokeWidth = 4.dp)
            }
        }
    }
}@Composable
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
        label = { Text(label, color = SoftCyan.copy(0.4f), fontSize = 12.sp) },
        modifier = Modifier.width(280.dp),
        leadingIcon = { Icon(icon, null, tint = NeonCyan.copy(0.7f), modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = NeonCyan.copy(0.5f),
                        modifier = Modifier.size(20.dp)
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