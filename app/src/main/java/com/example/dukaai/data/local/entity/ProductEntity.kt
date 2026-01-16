package com.example.dukaai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Product entity for Room database
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcode: String? = null,
    val category: String,
    val currentStock: Int = 0,
    val minStockThreshold: Int = 10,
    val buyingPrice: Double,
    val sellingPrice: Double,
    val unit: String = "pieces",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
