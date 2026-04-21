package com.management.inventorypro.models



data class ProductModel(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val category: String = "Uncategorized", // Adding this "= "Uncategorized"" fixes the error
    val customFields: Map<String, String> = emptyMap()
)
