package com.management.inventorypro.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("inventory")

    // This will hold the live count of items
    var inventoryCount by mutableStateOf(0)

    init {
        fetchInventoryCount()
    }

    private fun fetchInventoryCount() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // snapshot.childrenCount gives you the number of items in the "inventory" node
                inventoryCount = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }
}