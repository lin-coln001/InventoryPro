package com.management.inventorypro.ui.theme.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val database = com.google.firebase.database.FirebaseDatabase.getInstance()
    val uid = auth.currentUser?.uid

    // States
    var showAll by remember { mutableStateOf(true) }
    var fieldCountText by remember { mutableStateOf("2") }
    var isSaving by remember { mutableStateOf(false) }

    // Fetch current setting on load
    LaunchedEffect(uid) {
        if (uid != null) {
            database.getReference("users").child(uid).child("settings").child("maxVisibleFields")
                .get().addOnSuccessListener { snapshot ->
                    val value = snapshot.getValue(Int::class.java) ?: 0
                    if (value == 0) {
                        showAll = true
                    } else {
                        showAll = false
                        fieldCountText = value.toString()
                    }
                }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Display Settings") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // Toggle for Unlimited
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Show All Custom Fields", modifier = Modifier.weight(1f))
                Switch(checked = showAll, onCheckedChange = { showAll = it })
            }

            // Input for Specific Number (only enabled if showAll is false)
            if (!showAll) {
                OutlinedTextField(
                    value = fieldCountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) fieldCountText = it },
                    label = { Text("Number of fields to display") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (uid != null) {
                        isSaving = true
                        // If showAll is true, we save 0 to represent "Unlimited"
                        val finalValue = if (showAll) 0 else fieldCountText.toIntOrNull() ?: 2

                        database.getReference("users").child(uid).child("settings")
                            .child("maxVisibleFields").setValue(finalValue)
                            .addOnSuccessListener {
                                isSaving = false
                                navController.popBackStack()
                            }
                            .addOnFailureListener { isSaving = false }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Apply Preferences")
            }
        }
    }
}
@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navController = NavController(LocalContext.current))
}