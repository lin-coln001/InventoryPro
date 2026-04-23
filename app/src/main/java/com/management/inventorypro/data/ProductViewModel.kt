package com.management.inventorypro.data

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.management.inventorypro.models.CustomField
import com.management.inventorypro.models.ProductModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    // --- 1. STATE FLOW FOR PRODUCTS (Fixed Duplicates) ---
    private val _products = MutableStateFlow<List<ProductModel>>(emptyList())
    val products: StateFlow<List<ProductModel>> = _products.asStateFlow()

    // UI States
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var isUploading by mutableStateOf(false)
    val customFields = mutableStateListOf<CustomField>()

    init {
        fetchProducts()
    }

    private fun getUserId(): String? = auth.currentUser?.uid

    // --- 2. DATA FETCHING ---
    private fun fetchProducts() {
        val userId = getUserId() ?: return
        val inventoryRef = database.getReference("users").child(userId).child("inventory")

        inventoryRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ProductModel>()
                for (snap in snapshot.children) {
                    snap.getValue(ProductModel::class.java)?.let { list.add(it) }
                }
                _products.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- 3. FIELD MANAGEMENT ---
    fun addNewField() {
        customFields.add(CustomField("", ""))
    }

    fun removeField(index: Int) {
        if (index in customFields.indices) {
            customFields.removeAt(index)
        }
    }

    // --- 4. UPLOAD & SAVE LOGIC ---
    fun uploadProduct(productName: String, category: String, onComplete: () -> Unit) {
        val uid = getUserId() ?: return
        val uri = selectedImageUri ?: return

        isUploading = true

        MediaManager.get().upload(uri)
            .unsigned("image_folder")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) { }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) { }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url").toString()
                    saveToFirebase(uid, productName, category, imageUrl, onComplete)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    isUploading = false
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) { }
            }).dispatch()
    }

    private fun saveToFirebase(uid: String, name: String, category: String, imageUrl: String, onComplete: () -> Unit) {
        val userInventoryRef = database.getReference("users").child(uid).child("inventory").push()
        val productId = userInventoryRef.key ?: ""
        val fieldsMap = customFields.associate { it.key to it.value }

        val product = ProductModel(
            id = productId,
            name = name,
            category = category,
            imageUrl = imageUrl,
            customFields = fieldsMap
        )

        userInventoryRef.setValue(product).addOnCompleteListener {
            isUploading = false
            onComplete()
        }
    }

    fun fetchProductById(productId: String, onResult: (ProductModel) -> Unit) {
        val uid = getUserId() ?: return
        database.getReference("users").child(uid).child("inventory").child(productId)
            .get().addOnSuccessListener { snapshot ->
                val product = snapshot.getValue(ProductModel::class.java)
                product?.let { onResult(it) }
            }
    }
}