package com.management.inventorypro.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

class UpdateManager(private val context: Context) {

    fun downloadAndInstall(apkUrl: String) {
        val destination = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk")
        if (destination.exists()) destination.delete()

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("InventoryPro Update")
            .setDescription("Downloading latest version...")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // Use this specific method for better compatibility
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        // Listen for when the download finishes
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(destination)
                    context.unregisterReceiver(this)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                context,
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

    }
    suspend fun checkForUpdates(jsonUrl: String): Pair<Int, String>? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder().url(jsonUrl).build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val json = JSONObject(response.body?.string() ?: "")
                val latestVersion = json.getInt("latestVersionCode")
                val apkUrl = json.getString("url")

                return@withContext Pair(latestVersion, apkUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun installApk(file: File) {
        // 1. Check for "Install Unknown Apps" permission (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                // We return because the user needs to toggle the switch and then try again
                return
            }
        }

        // 2. Proceed with Installation
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Clear top ensures we don't have multiple installer instances
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Installation failed: ${e.message}")
        }
    }
}