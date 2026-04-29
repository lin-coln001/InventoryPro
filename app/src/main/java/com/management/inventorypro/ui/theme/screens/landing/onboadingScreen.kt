package com.management.inventorypro.ui.theme.screens.landing

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.management.inventorypro.models.inventoryQuestions


import com.management.inventorypro.ui.theme.NeonCyan
import com.management.inventorypro.ui.theme.SoftCyan
import com.management.inventorypro.ui.theme.SurfaceNavy

@Composable
fun OnboardingSurvey(
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(-1) }
    val selectedAnswers = remember { mutableStateListOf<Int?>(null, null, null, null, null) }

    // Calculate progress (0.0 to 1.0)
    val progress by animateFloatAsState(
        targetValue = if (currentStep >= 0) (currentStep + 1) / 5f else 0f,
        label = "SurveyProgress"
    )

    if (currentStep == -1) {
        AlertDialog(
            containerColor = SurfaceNavy,
            titleContentColor = NeonCyan,
            textContentColor = Color.White,
            onDismissRequest = { onSkip() },
            title = { Text("Personalize Your Experience", fontWeight = FontWeight.Bold) },
            text = { Text("Take 30 seconds to optimize your tracking engine. You can skip this if you're in a hurry.") },
            confirmButton = {
                TextButton(onClick = { currentStep = 0 }) {
                    Text("START", color = NeonCyan, fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onSkip() }) {
                    Text("SKIP", color = Color.White.copy(0.5f))
                }
            }
        )
    } else if (currentStep < inventoryQuestions.size) {
        val question = inventoryQuestions[currentStep]

        AlertDialog(
            containerColor = SurfaceNavy,
            onDismissRequest = { },
            title = {
                Column {
                    // --- THE PROGRESS BAR ---
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = NeonCyan,
                        trackColor = Color.White.copy(alpha = 0.1f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SYSTEM CONFIGURATION: ${currentStep + 1}/5",
                        fontSize = 12.sp,
                        color = SoftCyan,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = question.question,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    question.options.forEachIndexed { index, option ->
                        val isSelected = selectedAnswers[currentStep] == index

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonCyan.copy(0.15f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NeonCyan else Color.White.copy(0.05f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedAnswers[currentStep] = index }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedAnswers[currentStep] = index },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonCyan,
                                    unselectedColor = Color.White.copy(0.3f)
                                )
                            )
                            Text(
                                text = option,
                                color = if (isSelected) NeonCyan else Color.White,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedAnswers[currentStep] != null,
                    onClick = {
                        if (currentStep == 4) onComplete() else currentStep++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        disabledContainerColor = Color.White.copy(0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (currentStep == 4) "FINISH" else "NEXT",
                        color = SurfaceNavy,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                if (currentStep >= 0) {
                    TextButton(onClick = {
                        if (currentStep == 0) currentStep = -1 else currentStep--
                    }) {
                        Text("BACK", color = Color.White.copy(0.6f))
                    }
                }
            }
        )
    }
}