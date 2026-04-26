package com.management.inventorypro.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.management.inventorypro.models.UserModel


class AuthViewModel: ViewModel( ) {
    private val auth: FirebaseAuth =FirebaseAuth.getInstance()
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    fun signup(
        username:String,
        phone:String,
        email:String, password:String, confirmpassword:String, navController: NavController, context: Context
    ){
        if (username.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank() || confirmpassword.isBlank()){
            Toast.makeText(context,"Please fill all the fields", Toast.LENGTH_LONG).show()
            return
        }
        if (password != confirmpassword){
            Toast.makeText(context,"Password do not match",Toast.LENGTH_LONG).show()
            return
        }
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                task ->
            if (task.isSuccessful){
                val userId = auth.currentUser?.uid ?: ""
                val user =
                    UserModel(username = username, email = email, userId = userId, phone = phone)

                saveUserToDatabase(user,navController,context)
            }else{
                Toast.makeText(context,task.exception?.message ?:
                "Registration failed",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun saveUserToDatabase(user: UserModel, navController: NavController, context: Context){
        val dbRef = FirebaseDatabase.getInstance().getReference("User/${user.userId}")
        dbRef.setValue(user).addOnCompleteListener{
                task ->
            if (task.isSuccessful){
                Toast.makeText(context,"User Registered successfully",
                    Toast.LENGTH_LONG).show()
                navController.navigate("login"){
                    popUpTo(0)
                }
            }else{
                Toast.makeText(context,task.exception?.message ?: "Failed to save user",
                    Toast.LENGTH_LONG).show()
            }
        }


    }
    fun login(email: String,password: String,navController: NavController,context: Context){
        if (email.isBlank() || password.isBlank()){
            Toast.makeText(context,"Username and Password required",Toast.LENGTH_LONG).show()
            return
        }
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                task ->
            if (task.isSuccessful){
                Toast.makeText(context,"Login Successful",Toast.LENGTH_LONG).show()
                navController.navigate("dashboard"){
                    popUpTo(0)
                }
            }else{
                Toast.makeText(context,task.exception?.message ?: "Login failed",
                    Toast.LENGTH_LONG).show()
            }}}

    //    fun logout(navController: NavController) {
//
//        mAuth.signOut()
//
//        navController.navigate("login") {
//            // This clears the backstack so the user can't "Back" into the Dashboard
//            popUpTo(0) { inclusive = true }
//            launchSingleTop = true
//        }
//    }
    fun logout(navController: NavController,context: Context) {
        mAuth.signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navController.navigate("login") {
            popUpTo("dashboard") { inclusive = true }
        }
    }
    fun getUsername(onResult: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

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




}