package com.management.inventorypro

import android.animation.ObjectAnimator
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import com.cloudinary.android.MediaManager
import com.management.inventorypro.navigation.AppNavHost
import com.management.inventorypro.ui.theme.InventoryProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install the Splash Screen BEFORE super.onCreate
        val splashScreen = installSplashScreen()

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create a custom fade out for the system splash
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            )
            fadeOut.duration = 500L
            fadeOut.doOnEnd { splashScreenView.remove() }
            fadeOut.start()
        }

        super.onCreate(savedInstanceState)

        // 2. Initialize Cloudinary (Necessary for your images)
        val config = mapOf(
            "cloud_name" to "djtr5luf6",
            "secure" to true
        )

        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Log error if needed, but prevents crash on double-init
        }

        setContent {
            // Your App Theme and NavHost go here
            InventoryProTheme {
                AppNavHost()
            }
        }
    }
}
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
    // If HospitalManagementSystemTheme is missing, just call the composable directly
    Greeting("Android")
}
