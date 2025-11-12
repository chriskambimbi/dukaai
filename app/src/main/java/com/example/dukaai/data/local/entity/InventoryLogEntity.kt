package com.example.dukaai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Inventory Log entity for Room database
 * Tracks all stock movements
 */
@Entity(
    tableName = "inventory_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class InventoryLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val actionType: String, // "ADD", "REMOVE", "ADJUST", "SALE"
    val quantityChange: Int, // Positive for additions, negative for removals
    val previousStock: Int,
    val newStock: Int,
    val reason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
