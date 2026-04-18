package com.management.inventorypro.models

data class ProductModel(
    val id: String = "",
    val name: String= "",
    val imageUrl: String = "",

    val customFields: Map<String, String> = emptyMap()
)
