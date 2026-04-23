package com.management.inventorypro.data

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.management.inventorypro.models.CustomField
import com.management.inventorypro.models.ProductModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

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
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Fetch cancelled: ${error.message}")
            }
        })
    }

    fun addNewField() {
        customFields.add(CustomField("", ""))
    }

    fun removeField(index: Int) {
        if (index in customFields.indices) {
            customFields.removeAt(index)
        }
    }

    /**
     * CLOUDINARY UPLOAD LOGIC
     * This takes a URI and returns the secure HTTPS URL.
     */
    fun uploadToCloudinary(uri: Uri, onComplete: (String) -> Unit) {
        isUploading = true

        MediaManager.get().upload(uri)
            .unsigned("InventoryPro") // Ensure this matches your Cloudinary Upload Preset!
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    // Make sure you are grabbing "secure_url"
                    val permanentUrl = resultData?.get("secure_url")?.toString() ?: ""
                    Log.d("CloudinarySuccess", "URL: $permanentUrl") // Add this to see it in Logcat!
                    onComplete(permanentUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload failed: ${error?.description}")
                    isUploading = false // Reset loading on error
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    /**
     * FIREBASE SAVE LOGIC
     * Call this inside the onComplete block of uploadToCloudinary
     */
    fun saveProductToFirebase(
        name: String,
        category: String,
        imageUrl: String,
        onComplete: () -> Unit
    ) {
        val uid = getUserId() ?: return
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
            selectedImageUri = null // Clear image for next entry
            customFields.clear()    // Clear fields for next entry
            onComplete()
        }
    }
}