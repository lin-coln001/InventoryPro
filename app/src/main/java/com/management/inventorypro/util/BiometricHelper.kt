package com.management.inventorypro.util // Use your actual package path

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
class BiometricHelper(private val activity: FragmentActivity) {
    // Added (Boolean) -> Unit so we can pass 'true' or 'false' back
    fun authenticate(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true) // Pass true on success
                }


                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {

                        onResult(false)
                    } else {

                        onResult(false)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Optional: onResult(false) here if you want to close on single failed scan
                }

            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("System Access")
            .setSubtitle("Verify identity to proceed")
            .setNegativeButtonText("Use Password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}