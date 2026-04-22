package com.management.inventorypro.data

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.management.inventorypro.models.ProductModel
import kotlin.jvm.java


class InventoryViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()


    // State for the list of products
    var products = mutableStateListOf<ProductModel>()

    // State for display settings (0 = Show All)
    // We use mutableIntStateOf so the UI recomposes when this changes

    var maxFieldsToShow by mutableIntStateOf(2) // Default to 2 fields

    var isLoading by mutableStateOf(true)

    init {
        // Start both listeners as soon as the ViewModel is created
        fetchMaxFieldsSetting()
        fetchProducts()
    }

    /**
     * Listen for changes to the user's specific display preferences.
     * Path: users/$uid/settings/maxVisibleFields
     */
    private fun fetchMaxFieldsSetting() {
        val uid = auth.currentUser?.uid ?: return
        val settingsRef = database.getReference("users")
            .child(uid)
            .child("settings")
            .child("maxVisibleFields")

        settingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Update our state variable. Default to 0 (Unlimited) if not found.
                maxFieldsToShow = snapshot.getValue(Int::class.java) ?: 0
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors if necessary
            }
        })
    }

    /**
     * Listen for changes to the user's specific inventory list.
     * Path: users/$uid/inventory
     */
    private fun fetchProducts() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            isLoading = false
            return
        }

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

    /**
     * Deletes a product from the user's specific inventory path.
     */
    fun deleteProduct(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference("users")
            .child(uid)
            .child("inventory")
            .child(productId)
            .removeValue()
    }
}