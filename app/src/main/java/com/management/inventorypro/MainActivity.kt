package com.management.inventorypro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cloudinary.android.MediaManager
import com.management.inventorypro.navigation.AppNavHost
import com.management.inventorypro.ui.theme.InventoryProTheme
import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy
import com.management.inventorypro.util.UpdateManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Cloudinary
        val config = mapOf("cloud_name" to "djtr5luf6", "secure" to true)
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Initialization already handled
        }

        setContent {
            InventoryProTheme {
                val context = LocalContext.current
                val updateManager = remember { UpdateManager(context) }

                // --- MANUAL VERSION TRACKING ---
                // Change this number manually when you push a new APK to GitHub
                val currentVersion = BuildConfig.VERSION_CODE


                var updateData by remember { mutableStateOf<Pair<Int, String>?>(null) }

                // Check GitHub for updates
                LaunchedEffect(Unit) {
                    try {
                        // Adding a timestamp at the end avoids GitHub's 5-minute cache
                        val jsonUrl = "https://raw.githubusercontent.com/lin-coln001/InventoryPro/master/update.json?nocache=${System.currentTimeMillis()}"
                        val info = updateManager.checkForUpdates(jsonUrl)

                        android.util.Log.d("UpdateCheck", "Local: $currentVersion, Remote: ${info?.first}")

                        if (info != null && info.first > currentVersion) {
                            updateData = info
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("UpdateCheck", "Error: ${e.message}")
                    }
                }

                // Update Dialog
                updateData?.let { data ->
                    AlertDialog(
                        containerColor = SurfaceNavy,
                        titleContentColor = NeonCyan,
                        textContentColor = Color.White,
                        onDismissRequest = { updateData = null },
                        title = { Text("Update Available", fontWeight = FontWeight.Bold) },
                        text = { Text("A new version of InventoryPro is ready. Would you like to install it now?") },
                        confirmButton = {
                            TextButton(onClick = {
                                updateManager.downloadAndInstall(data.second)
                                updateData = null
                            }) {
                                Text("UPDATE", color = NeonCyan, fontWeight = FontWeight.ExtraBold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateData = null }) {
                                Text("LATER", color = SoftCyan.copy(0.6f))
                            }
                        }
                    )
                }

                // Main App UI
                AppNavHost()
            }
        }
    }
}
