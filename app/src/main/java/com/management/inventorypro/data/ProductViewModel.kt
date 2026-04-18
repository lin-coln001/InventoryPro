package com.management.inventorypro.data


import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.models.CustomField
import com.management.inventorypro.models.ProductModel

class ProductViewModel : ViewModel() {

    // 1. State for Dynamic Fields
    val customFields = mutableStateListOf<CustomField>()

    // 2. State for the Selected Image
    var selectedImageUri by mutableStateOf<Uri?>(null)

    // 3. Loading State (Useful for showing a spinner in the UI)
    var isUploading by mutableStateOf(false)

    /**
     * Main function called by the UI
     */
    fun uploadProduct(productName: String, onSuccess: () -> Unit) {
        isUploading = true

        val uri = selectedImageUri
        if (uri != null) {
            // Here we use the non-null 'uri'
            uploadImageToCloudinary(uri) { imageUrl ->
                saveProductToFirebase(productName, imageUrl, onSuccess)
            }
        } else {
            saveProductToFirebase(productName, "", onSuccess)
        }
    }

    /**
     * Step 1: Upload the raw file to Cloudinary
     */
    private fun uploadImageToCloudinary(uri: Uri, onUrlReady: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .unsigned("image_folder") // REPLACE with your preset
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val secureUrl = resultData?.get("secure_url").toString()
                    onUrlReady(secureUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    isUploading = false
                    // You could add a "errorMessage" state here to show in UI
                }

                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    /**
     * Step 2: Save the final Product object to Firebase
     */
    private fun saveProductToFirebase(name: String, imageUrl: String, onSuccess: () -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("inventory")
        val productId = database.push().key ?: return

        // Convert UI list of fields to a Map for Firebase
        val fieldsMap = customFields
            .filter { it.key.isNotBlank() }
            .associate { it.key to it.value }

        val product = ProductModel(
            id = productId,
            name = name,
            imageUrl = imageUrl,
            customFields = fieldsMap
        )

        database.child(productId).setValue(product)
            .addOnSuccessListener {
                isUploading = false
                customFields.clear()
                selectedImageUri = null
                onSuccess()
            }
            .addOnFailureListener {
                isUploading = false
            }
    }

    fun addNewField() {
        customFields.add(CustomField())
    }
}