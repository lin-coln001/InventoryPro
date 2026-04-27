//package com.management.inventorypro.util
//
//import android.app.DownloadManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.net.Uri
//import android.os.Build
//import android.os.Environment
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONObject
//import java.io.File
//
//class UpdateManager(private val context: Context) {
//
//    fun downloadAndInstall(apkUrl: String) {
//        val destination = File(
//            context.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS)[0],
//            "update.apk"
//        )
//        if (destination.exists()) destination.delete()
//
//        val request = DownloadManager.Request(Uri.parse(apkUrl))
//            .setTitle("InventoryPro Update")
//            .setDescription("Downloading latest version...")
//            .setMimeType("application/vnd.android.package-archive")
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            // Use this specific method for better compatibility
//            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk")
//            .setAllowedOverMetered(true)
//            .setAllowedOverRoaming(true)
//
//        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        val downloadId = manager.enqueue(request)
//
//        // Listen for when the download finishes
//        val onComplete = object : BroadcastReceiver() {
//            override fun onReceive(ctx: Context?, intent: Intent?) {
//                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//                if (id == downloadId) {
//                    installApk(destination)
//                    context.unregisterReceiver(this)
//                }
//            }
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            context.registerReceiver(
//                onComplete,
//                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
//                Context.RECEIVER_NOT_EXPORTED
//            )
//        } else {
//            ContextCompat.registerReceiver(
//                context,
//                onComplete,
//                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
//                ContextCompat.RECEIVER_NOT_EXPORTED
//            )
//        }
//
//    }
//    suspend fun checkForUpdates(jsonUrl: String): Pair<Int, String>? = withContext(Dispatchers.IO) {
//        val client = OkHttpClient()
//        val request = Request.Builder().url(jsonUrl).build()
//
//        try {
//            client.newCall(request).execute().use { response ->
//                if (!response.isSuccessful) return@withContext null
//
//                val json = JSONObject(response.body?.string() ?: "")
//                val latestVersion = json.getInt("latestVersionCode")
//                val apkUrl = json.getString("url")
//
//                return@withContext Pair(latestVersion, apkUrl)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    private fun installApk(file: File) {
//        // 1. Check for "Install Unknown Apps" permission (Android 8.0/Oreo and above)
//        // This is required before the app can even trigger the installation dialog.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (!context.packageManager.canRequestPackageInstalls()) {
//                val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
//                    data = Uri.parse("package:${context.packageName}")
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                }
//                context.startActivity(intent)
//                // We return because the user must toggle the switch and then try the update again.
//                return
//            }
//        }
//
//        // 2. Prepare the installation Intent
//        try {
//            // Use the hardcoded authority string that matches your AndroidManifest.xml
//            val authority = "com.management.inventorypro.fileprovider"
//
//            val uri = FileProvider.getUriForFile(
//                context,
//                authority,
//                file
//            )
//
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                // Set the data and the specific APK MIME type
//                setDataAndType(uri, "application/vnd.android.package-archive")
//
//                // Grant temporary read permission to the system installer
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//                // Required because we are calling from outside an Activity context (the receiver)
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//                // Ensures that if the installer is already open, it refreshes with this new file
//                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            }
//
//            // 3. Launch the Installer
//            context.startActivity(intent)
//
//        } catch (e: Exception) {
//            // This will catch issues like "File Not Found" or "FileProvider not found in Manifest"
//            android.util.Log.e("UpdateManager", "Installation failed: ${e.message}")
//            e.printStackTrace()
//        }
//    }
//
//}
package com.management.inventorypro.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

class UpdateManager(private val activity: Activity) {

    // -------------------------
    // CHECK FOR UPDATES
    // -------------------------
    suspend fun checkForUpdates(jsonUrl: String): Pair<Int, String>? =
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(jsonUrl)
                    .header("Cache-Control", "no-cache")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null

                    val json = JSONObject(response.body?.string() ?: "")
                    val version = json.getInt("latestVersionCode")
                    val url = json.getString("url")

                    Pair(version, url)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    // -------------------------
    // DOWNLOAD APK (WITH PROGRESS)
    // -------------------------
    suspend fun downloadApk(
        apkUrl: String,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {

        try {
            val file = File(activity.filesDir, "update.apk")

            val client = OkHttpClient()
            val request = Request.Builder().url(apkUrl).build()

            client.newCall(request).execute().use { response ->

                if (!response.isSuccessful) return@withContext null

                val body = response.body ?: return@withContext null
                val total = body.contentLength()
                val unknownSize = total <= 0

                var downloaded = 0L

                body.byteStream().use { input ->
                    file.outputStream().use { output ->

                        val buffer = ByteArray(8 * 1024)
                        var read: Int

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read

                            val progress = if (!unknownSize) {
                                ((downloaded * 100) / total).toInt()
                            } else {
                                -1
                            }

                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // -------------------------
    // INSTALL APK (FIXED + SAFE)
    // -------------------------
    fun installApk(file: File) {

        if (!file.exists()) return

        // Android 8+ permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val allowed = activity.packageManager.canRequestPackageInstalls()

            if (!allowed) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
                return
            }
        }

        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        activity.startActivity(intent)
    }
}