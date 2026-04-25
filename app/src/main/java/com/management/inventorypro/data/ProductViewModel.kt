package com.management.inventorypro.data

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.management.inventorypro.models.CustomField
import com.management.inventorypro.models.ProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductViewModel(application: Application) : AndroidViewModel(application){
    private val auth = FirebaseAuth.getInstance()

    private val context = application.applicationContext
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
                viewModelScope.launch(Dispatchers.Default) {
                    val list = mutableListOf<ProductModel>()
                    for (snap in snapshot.children) {
                        snap.getValue(ProductModel::class.java)?.let { list.add(it) }
                    }

                    // 1. Pre-fetch images while STILL on the background thread
                    list.take(8).forEach { product -> // Reduced to 8 for lower-end RAM
                        if (product.imageUrl.isNotEmpty()) {
                            val request = ImageRequest.Builder(context)
                                .data(product.imageUrl)
                                .size(200, 200) // Smaller thumbnails = faster initial load
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build()

                            // Enqueue on the background thread to keep UI smooth
                            Coil.imageLoader(context).enqueue(request)
                        }
                    }

                    // 2. ONLY switch to Main to update the UI state
                    withContext(Dispatchers.Main) {
                        _products.value = list
                    }
                }
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
     */
    fun uploadToCloudinary(uri: Uri, onComplete: (String) -> Unit) {
        isUploading = true
        MediaManager.get().upload(uri)
            .unsigned("InventoryPro") // Ensure this matches your Preset name
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val permanentUrl = resultData?.get("secure_url")?.toString() ?: ""
                    isUploading = false
                    onComplete(permanentUrl)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload failed: ${error?.description}")
                    isUploading = false
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    /**
     * FIREBASE SAVE LOGIC
     */
    fun saveProductToFirebase(
        productId: String? = null,
        name: String,
        category: String,
        imageUrl: String,
        onComplete: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        val ref = if (!productId.isNullOrEmpty()) {
            database.getReference("users").child(uid).child("inventory").child(productId)
        } else {
            database.getReference("users").child(uid).child("inventory").push()
        }

        val finalId = productId ?: ref.key ?: ""
        val product = ProductModel(
            id = finalId,
            name = name,
            category = category,
            imageUrl = imageUrl,
            customFields = customFields.associate { it.key to it.value }
        )

        ref.setValue(product).addOnSuccessListener {
            isUploading = false
            selectedImageUri = null // <--- ADD THIS LINE
            customFields.clear()
            onComplete()
        }
    }

    /**
     * PROFILE PICTURE UPLOAD
     */
    fun uploadProfilePicture(uri: Uri, onComplete: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        isUploading = true

        MediaManager.get().upload(uri)
            .unsigned("InventoryPro") // Use your preset name here too
            .option("folder", "profiles")
            .option("public_id", uid)
            // Removed .option("overwrite", true) to avoid the error
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url")?.toString() ?: ""
                    isUploading = false
                    onComplete(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    isUploading = false
                    Log.e("Cloudinary", "Profile Upload failed: ${error?.description}")
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }
} // End of class