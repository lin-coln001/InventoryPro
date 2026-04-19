package com.management.inventorypro.data

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // State variables for the Dashboard UI
    var totalItems by mutableIntStateOf(0)
    var isLoading by mutableStateOf(true)

    init {
        fetchDashboardData()
    }

    private fun fetchDashboardData() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            isLoading = false
            return
        }

        // CRITICAL: Pointing to the new user-specific path
        val userInventoryRef = database.getReference("users").child(uid).child("inventory")

        userInventoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot.childrenCount gives you the number of items in the inventory
                totalItems = snapshot.childrenCount.toInt()
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }
}