package com.management.inventorypro.models

data class SurveyQuestion(
    val question: String,
    val options: List<String>
)

val inventoryQuestions = listOf(
    SurveyQuestion("What is your primary use case?", listOf("Personal", "Small Business", "Enterprise")),
    SurveyQuestion("Current inventory size?", listOf("1-50 items", "51-500 items", "500+ items")),
    SurveyQuestion("Do you require barcode scanning?", listOf("Yes, frequently", "Occasionally", "No")),
    SurveyQuestion("Preferred update frequency?", listOf("Real-time", "Daily", "Weekly")),
    SurveyQuestion("How did you hear about us?", listOf("Social Media", "Friend/Colleague", "Other"))
)
