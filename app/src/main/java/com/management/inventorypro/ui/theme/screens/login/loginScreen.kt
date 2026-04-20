package com.management.inventorypro.ui.theme.screens.login

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import com.management.inventorypro.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.management.inventorypro.data.AuthViewModel
import com.management.inventorypro.ui.theme.screens.login.LoginScreen


@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    // Initialize rememberMe from storage
    val sharedPref = remember { context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE) }
    var rememberMe by remember { mutableStateOf(sharedPref.getBoolean("remember", false)) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Keeps everything centered vertically
    ) {
        AsyncImage(
            model = R.drawable.hello, // Your GIF name
            imageLoader = imageLoader,     // Use the loader we just made
            contentDescription = "Login Animation",
            modifier = Modifier
                .size(140.dp)
                .clip(RectangleShape),
//                .border(2.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "Log in Here",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        Spacer(modifier = Modifier.size(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "enter email") },
            placeholder = { Text(text = "Please enter your email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )

        Spacer(modifier = Modifier.size(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "enter your password") },
            placeholder = { Text(text = "please enter your password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
        )

        // --- SIDE BY SIDE CHECKBOX ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .width(280.dp) // Matches the width of the TextFields
                .padding(vertical = 8.dp)
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
            Text(
                text = "Remember Me",
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.clickable { rememberMe = !rememberMe }
            )
        }

        Button(
            onClick = {
                sharedPref.edit().putBoolean("remember", rememberMe).apply()
                authViewModel.login(email, password, navController, context)
            },
            modifier = Modifier.width(280.dp) // Consistent button width
        ) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.size(24.dp))

        // --- FOOTER SECTION (Now inside the Column) ---
        Row {
            Text(text = "Don't have an account? ", color = Color.Red)
            Text(
                text = "Register Here",
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { navController.navigate("register") }
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = "forgot password?",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.clickable { navController.navigate("dashboard") }
        )
    }
}
@Preview(showBackground= true, showSystemUi = true)
@Composable
fun LoginScreenPreview(){
    LoginScreen(rememberNavController())
}