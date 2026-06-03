package com.nvtrong.doanchilltea.model

data class Product(
    val id: Int,
    val name: String,
    val price: String,
    val imageUrl: String?,
    val categoryId: Int? = null,
    val categoryName: String? = null
)
