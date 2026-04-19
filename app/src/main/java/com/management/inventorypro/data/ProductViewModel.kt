package com.management.inventorypro.data

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.models.CustomField
import com.management.inventorypro.models.ProductModel

class ProductViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    var selectedImageUri by mutableStateOf<Uri?>(null)
    var isUploading by mutableStateOf(false)
    val customFields = mutableStateListOf<CustomField>()

    private fun getUserId(): String? = auth.currentUser?.uid

    fun addNewField() {
        customFields.add(CustomField("", ""))
    }

    fun removeField(index: Int) {
        if (index in customFields.indices) {
            customFields.removeAt(index)
        }
    }

    /**
     * Handles the sequential flow:
     * 1. Upload image to Cloudinary
     * 2. Receive URL
     * 3. Save Name + URL + CustomFields to user-specific Firebase path
     */
    fun uploadProduct(productName: String, onComplete: () -> Unit) {
        val uid = getUserId() ?: return
        val uri = selectedImageUri ?: return // Ensure image exists

        isUploading = true

        // Replace "your_upload_preset" with the unsigned preset from your Cloudinary console
        MediaManager.get().upload(uri)
            .unsigned("image_folder")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) { }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) { }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    // WE HAVE THE URL! Now save to Firebase
                    val imageUrl = resultData?.get("secure_url").toString()
                    saveToFirebase(uid, productName, imageUrl, onComplete)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    isUploading = false
                    // Logic for error handling (Toasts, etc.)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) { }
            }).dispatch()
    }

    private fun saveToFirebase(uid: String, name: String, imageUrl: String, onComplete: () -> Unit) {
        val userInventoryRef = database.getReference("users").child(uid).child("inventory").push()
        val productId = userInventoryRef.key ?: ""

        // Convert list of CustomField objects to a Map for Firebase
        val fieldsMap = customFields.associate { it.key to it.value }

        val product = ProductModel(
            id = productId,
            name = name,
            imageUrl = imageUrl,
            customFields = fieldsMap
        )

        userInventoryRef.setValue(product).addOnCompleteListener {
            isUploading = false
            onComplete()
        }
    }

    // UPDATED: Fetches only the product belonging to the logged-in user
    fun fetchProductById(productId: String, onResult: (ProductModel) -> Unit) {
        val uid = getUserId() ?: return
        database.getReference("users").child(uid).child("inventory").child(productId)
            .get().addOnSuccessListener { snapshot ->
                val product = snapshot.getValue(ProductModel::class.java)
                product?.let { onResult(it) }
            }
    }

    // UPDATED: Updates the product in the user-specific path
    fun updateProduct(productId: String, name: String, onComplete: () -> Unit) {
        val uid = getUserId() ?: return
        isUploading = true

        val fieldsMap = customFields.associate { it.key to it.value }
        val updates = mapOf(
            "name" to name,
            "customFields" to fieldsMap
        )

        database.getReference("users").child(uid).child("inventory").child(productId)
            .updateChildren(updates).addOnCompleteListener {
                isUploading = false
                onComplete()
            }
    }
}