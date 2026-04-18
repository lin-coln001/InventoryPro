package com.management.inventorypro


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cloudinary.android.MediaManager

import com.management.inventorypro.navigation.AppNavHost
import com.management.inventorypro.ui.theme.InventoryProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Cloudinary (Only do this once!)
        val config = mapOf(
            "cloud_name" to "djtr5luf6",
            "secure" to true
        )
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Already initialized
        }

        setContent {
            // Your NavHost or Screen
            AppNavHost( )
        }
    }}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    InventoryProTheme( ) {
        Greeting("android")
    }
}
