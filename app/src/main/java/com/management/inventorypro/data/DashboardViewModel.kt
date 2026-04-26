package com.management.inventorypro.data

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // UI States - Initialized with "Loading" values to prevent layout jumps
    var totalItems by mutableIntStateOf(0)
    var userName by mutableStateOf("...")
    var isLoading by mutableStateOf(true)

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            isLoading = false
            return
        }

        isLoading = true

        // Use a Coroutine to fire off both requests in parallel
        viewModelScope.launch(Dispatchers.IO) {
            fetchUsername(uid)
            fetchTotalItemCount(uid)
        }
    }

    private fun fetchUsername(uid: String) {
        val primaryPath = database.getReference("User").child(uid).child("username")
        val secondaryPath = database.getReference("users").child(uid).child("profile/username")

        // Try the primary path first (User/uid/username)
        primaryPath.get().addOnSuccessListener { snapshot ->
            val name = snapshot.getValue(String::class.java)

            if (name != null) {
                updateUsernameUI(name)
            } else {
                // If primary is empty, try the secondary path as a backup
                secondaryPath.get().addOnSuccessListener { secondSnap ->
                    val backupName = secondSnap.getValue(String::class.java) ?: "Inventory User"
                    updateUsernameUI(backupName)
                }
            }
        }.addOnFailureListener {
            // If the whole request fails, try the secondary path
            secondaryPath.get().addOnSuccessListener { secondSnap ->
                val backupName = secondSnap.getValue(String::class.java) ?: "Inventory User"
                updateUsernameUI(backupName)
            }
        }
    }

    private fun updateUsernameUI(name: String) {
        viewModelScope.launch(Dispatchers.Main) {
            userName = name
            checkLoadingStatus()
        }
    }

    private fun fetchTotalItemCount(uid: String) {
        val inventoryRef = database.getReference("users").child(uid).child("inventory")

        // .get() is faster for initial dashboard load than a constant listener
        inventoryRef.get().addOnSuccessListener { snapshot ->
            // snapshot.childrenCount is a metadata fetch (very light on RAM)
            val total = snapshot.childrenCount.toInt()

            viewModelScope.launch(Dispatchers.Main) {
                totalItems = total
                checkLoadingStatus()
            }
        }.addOnFailureListener {
            Log.e("Dashboard", "Failed to get count", it)
            viewModelScope.launch(Dispatchers.Main) { isLoading = false }
        }
    }

    private fun checkLoadingStatus() {
        // If we have data (or at least finished the name fetch), stop showing the spinner
        if (userName != "...") {
            isLoading = false
        }
    }
}