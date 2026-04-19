package com.management.inventorypro.data

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.management.inventorypro.models.ProductModel

class InventoryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    var products = mutableStateListOf<ProductModel>()
    var isLoading by mutableStateOf(true)

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            isLoading = false
            return
        }

        // UPDATED: Listener points to users/[uid]/inventory
        val userRef = database.getReference("users").child(uid).child("inventory")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                products.clear()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductModel::class.java)
                    product?.let { products.add(it) }
                }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    // UPDATED: Deletes from the user-specific path
    fun deleteProduct(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users").child(uid).child("inventory").child(productId)
            .removeValue()
    }

}