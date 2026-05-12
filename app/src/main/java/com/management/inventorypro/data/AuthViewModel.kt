package com.management.inventorypro.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.models.UserModel

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signup(
        username: String,
        phone: String,
        email: String,
        password: String,
        confirmpassword: String,
        navController: NavController,
        context: Context,
        onComplete: (Boolean) -> Unit // Added to match login pattern
    ) {
        if (username.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank() || confirmpassword.isBlank()) {
            Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG).show()
            onComplete(false)
            return
        }
        if (password != confirmpassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
            onComplete(false)
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: ""
                val user = UserModel(username = username, email = email, userId = userId, phone = phone)

                // Save credentials for Biometric use later
                val prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putString("saved_email", email)
                    putString("saved_password", password) // Use the 'password' variable
                    apply()
                }

                saveUserToDatabase(user, navController, context, onComplete)
            } else {
                Toast.makeText(context, task.exception?.message ?: "Registration failed", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }

    private fun saveUserToDatabase(user: UserModel, navController: NavController, context: Context, onComplete: (Boolean) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance().getReference("User/${user.userId}")
        dbRef.setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "User Registered successfully", Toast.LENGTH_LONG).show()
                onComplete(true)
                navController.navigate("login") {
                    popUpTo(0)
                }
            } else {
                Toast.makeText(context, task.exception?.message ?: "Failed to save user", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }

    fun login(
        email: String,
        pass: String,
        nav: NavController,
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Save credentials for Biometric Login capability
                    val prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("saved_email", email)
                        putString("saved_password", pass)
                        apply()
                    }

                    nav.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                    onComplete(true)
                } else {
                    Toast.makeText(context, "Auth Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
    }

    fun logout(navController: NavController, context: Context) {
        auth.signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navController.navigate("login") {
            popUpTo("dashboard") { inclusive = true }
        }
    }

    fun getUsername(onResult: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult("User")
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("User/$userId")
        dbRef.get().addOnSuccessListener { snapshot ->
            val username = snapshot.child("username").value?.toString() ?: "User"
            onResult(username)
        }.addOnFailureListener {
            onResult("User")
        }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Please enter your email address.")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Failed to send reset email")
                }
            }
    }
}